package ru.ifmo.loboda.net;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class Broadcaster implements Runnable {
    public static final long SLEEPTIME = 5000;
    private int port;

    public Broadcaster(int port){
        this.port = port;
    }

    public void run() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            DatagramSocket socket = new DatagramSocket();
            while (true) {
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface network = interfaces.nextElement();
                        List<InterfaceAddress> addresses = network.getInterfaceAddresses();
                        for (InterfaceAddress ia : addresses) {
                            if (ia.getBroadcast() != null) {
                                Packet info = new Packet(network.getHardwareAddress(), hostname, System.currentTimeMillis());
                                byte[] data = info.getBytes();
                                DatagramPacket packet = new DatagramPacket(data, data.length, ia.getBroadcast(), port);
                                socket.send(packet);
                            }
                        }
                    }
                    Thread.sleep(SLEEPTIME);
                } catch (IOException ignored) {} catch (InterruptedException e) {
                    System.err.println("Interrupted");
                    System.exit(1);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Hostname is unknown");
            System.exit(1);
        } catch (SocketException e) {
            System.err.println("Can't create socket");
            System.exit(1);
        }
    }
}
