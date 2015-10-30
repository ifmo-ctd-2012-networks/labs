package ru.network.layer;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.MacAddress;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author victor
 */
public class TransportLayer {
    private final Logger log = LoggerFactory.getLogger(TransportLayer.class);
    private final static int PACKET_LENGTH = 1024;
    private InetAddress inetAddress;
    private MacAddress macAddress;
    private ClientSender clientSender;
    private Broadcaster broadcaster;
    private Map<Integer, Runnable> receivers = new HashMap<>();
    private AtomicBoolean running = new AtomicBoolean(false);

    public boolean connect() {
        if (running.compareAndSet(false, true)) {
            List<Pair<byte[], InetAddress>> list = getBroadcast();
            if (list.isEmpty()) {
                return false;
            }
            macAddress = new MacAddress(list.get(0).getKey());
            inetAddress = list.get(0).getValue();
            log.info("Connected to network [mac=" + macAddress + ", ip=" + inetAddress + "]");
            clientSender = new ClientSender();
            new Thread(clientSender, Thread.currentThread().getName() + "-sender").start();
            broadcaster = new Broadcaster();
            new Thread(broadcaster, Thread.currentThread().getName() + "-broadcaster").start();
            return true;
        } else {
            return false;
        }
    }

    protected void send(InetSocketAddress inetSocketAddress, String message) {
        clientSender.send(inetSocketAddress, message);
    }

    protected void broadcast(InetSocketAddress inetSocketAddress, String message) {
        broadcaster.send(inetSocketAddress, message);
    }

    private static List<Pair<byte[], InetAddress>> getBroadcast() {
        List<Pair<byte[], InetAddress>> list = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getHardwareAddress() != null) {
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress address = interfaceAddress.getBroadcast();
                        if (address != null) {
                            list.add(new Pair<>(networkInterface.getHardwareAddress(), address));
                        }
                    }
                }
            }
            return list;
        } catch (SocketException e) {
            return Collections.emptyList();
        }
    }

    protected void bind(int port, BiConsumer<InetAddress, String> listener, boolean broadcast) {
        assert !receivers.containsKey(port);
        if (broadcast) {
            BroadcastReceiver receiver = new BroadcastReceiver(port, listener);
            receivers.put(port, receiver);
            new Thread(receiver, Thread.currentThread().getName() + "-breceiver").start();
        } else {
            ClientReceiver receiver = new ClientReceiver(port, listener);
            receivers.put(port, receiver);
            new Thread(receiver, Thread.currentThread().getName() + "-creceiver").start();
        }
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    private class ClientSender implements Runnable {

        private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        @Override
        public void run() {
            while (running.get()) {
                try {
                    Runnable runnable = queue.take();
                    runnable.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(InetSocketAddress inetSocketAddress, String message) {
            try {
                queue.put(() -> {
                    //log.debug("Send to " + inetSocketAddress);
                    try (Socket socket = new Socket(inetSocketAddress.getAddress(), inetSocketAddress.getPort())) {
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        outputStream.writeUTF(message);
                    } catch (ConnectException e) {
                        //e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private class Broadcaster implements Runnable {

        private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        @Override
        public void run() {
            while (running.get()) {
                try {
                    Runnable runnable = queue.take();
                    runnable.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(InetSocketAddress inetSocketAddress, String message) {
            try {
                queue.put(() -> {
                    //log.debug("broadcast " + message);
                    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setBroadcast(true);
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, inetSocketAddress);
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class BroadcastReceiver implements Runnable {

        private final int port;
        private final BiConsumer<InetAddress, String> listener;

        public BroadcastReceiver(int port, BiConsumer<InetAddress, String> listener) {
            this.port = port;
            this.listener = listener;
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                while (running.get()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
                        socket.receive(packet);
                        InetAddress address = packet.getAddress();
                        if (listener != null) {
                            listener.accept(address, new String(packet.getData(), StandardCharsets.UTF_8));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientReceiver implements Runnable {

        private final int port;
        private final BiConsumer<InetAddress, String> listener;

        public ClientReceiver(int port, BiConsumer<InetAddress, String> listener) {
            this.port = port;
            this.listener = listener;
        }

        @Override
        public void run() {
            try (ServerSocket socket = new ServerSocket(port)) {
                //log.debug("ServerSocket started at " + port);
                while (running.get()) {
                    try {
                        Socket client = socket.accept();
                        InetAddress inetAddress = client.getInetAddress();
                        DataInputStream inputStream = new DataInputStream(client.getInputStream());
                        String data = inputStream.readUTF();
                        if (listener != null) {
                            listener.accept(inetAddress, data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //log.debug(e.getMessage() + " " + port);
            }
        }
    }
}
