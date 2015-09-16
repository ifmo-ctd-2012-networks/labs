package ru.ifmo.ctddev.isaev.networking;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Ilya Isaev
 */
public class Broadcaster implements Runnable {
    public static final int PACKET_LENGTH = 128;
    private static final Executor executor = Executors.newFixedThreadPool(2);
    public static int PORT = 4445;
    public static String HOSTNAME;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected two arguments: <hostname> <port>");
        }
        PORT = Integer.parseInt(args[1]);
        HOSTNAME = args[0];
        executor.execute(new Broadcaster());
        executor.execute(new Receiver());
    }

    public static InetAddress getBroadcastAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }
            for (InterfaceAddress interfaceAddress :
                    networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast != null) {
                    return broadcast;
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            InetAddress broadCastAddress = getBroadcastAddress();
            DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getLocalHost());
            socket.setBroadcast(true);
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            byte[] host = HOSTNAME.getBytes(StandardCharsets.UTF_8);
            ByteBuffer header = ByteBuffer.allocate(mac.length + 1 + host.length);
            header.put(mac);
            header.put((byte) host.length);
            header.put(host);
            while (true) {
                ByteBuffer toSend = ByteBuffer.allocate(PACKET_LENGTH);
                toSend.put(header.array());
                toSend.putLong(System.currentTimeMillis());
                DatagramPacket packet = new DatagramPacket(toSend.array(), toSend.array().length, broadCastAddress, 4445);
                socket.send(packet);
                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
