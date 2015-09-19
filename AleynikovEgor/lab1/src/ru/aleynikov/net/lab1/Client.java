package ru.aleynikov.net.lab1;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class Client implements Runnable {
    public static final long TO_SLEEP = 5000;
    private int portNum;

    public Client(int portNum) {
        this.portNum = portNum;
    }

    @Override
    public void run() {
        try (DatagramSocket ds = new DatagramSocket()) {
            String hostname = InetAddress.getLocalHost().getHostName();
            while (true) {
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface network = interfaces.nextElement();
                        byte[] ha = network.getHardwareAddress();
                        if (ha != null) {
                            List<InterfaceAddress> addresses = network.getInterfaceAddresses();
                            for (InterfaceAddress ia : addresses) {
                                if (ia.getBroadcast() != null) {
                                    Info.Message info = new Info.Message(ha, hostname, System.currentTimeMillis());
                                    byte[] data = info.getBytes();
                                    DatagramPacket dp = new DatagramPacket(data, data.length, ia.getBroadcast(), portNum);
                                    ds.send(dp);
                                }
                            }
                        }
                    }
                    Thread.sleep(TO_SLEEP);
                } catch (IOException | InterruptedException e) {
                    System.err.println(e);
                    System.exit(1);
                }
            }
        } catch (UnknownHostException | SocketException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}
