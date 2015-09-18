package lab1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client extends Thread {
    private final int port;
    private final DatagramSocket socket = new DatagramSocket();
    private final List<Message> messages = new ArrayList<>();
    private final List<InetAddress> addresses = new ArrayList<>();
    private volatile boolean stopped;

    public Client(int port) throws SocketException, UnknownHostException {
        this.port = port;
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface i : interfaces) {
            if (i.getHardwareAddress() != null) {
                List<InterfaceAddress> add = i.getInterfaceAddresses();
                for (InterfaceAddress a : add) {
                    if (a.getBroadcast() != null) {
                        messages.add(new Message(i.getHardwareAddress(), InetAddress.getLocalHost().getHostName(), 0));
                        addresses.add(a.getBroadcast());
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            for (int i = 0; i < messages.size(); i++) {
                long time = System.currentTimeMillis();
                Message message = messages.get(i);
                message.timeStamp = time / 1000l;
                try {
                    byte[] messageBytes = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, addresses.get(i), port);
                    socket.send(packet);
                } catch (UnsupportedEncodingException e) {
                    if (!stopped)
                        System.err.println("Unsupported encoding: " + e.getMessage());
                } catch (UnknownHostException e) {
                    if (!stopped)
                        System.err.println("Unknown host: " + e.getMessage());
                } catch (IOException e) {
                    if (!stopped)
                        System.err.println("Error while sending message: " + e.getMessage());
                }
                while (System.currentTimeMillis() < time + 5000) {
                    try {
                        Thread.sleep(time + 5000 - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        if (stopped) {
                            return;
                        }
                    }
                }
            }
        }
    }

    public void close() {
        stopped = true;
        socket.close();
        interrupt();
    }
}
