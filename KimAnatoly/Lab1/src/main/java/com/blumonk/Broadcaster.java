package com.blumonk;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * @author akim
 */
public class Broadcaster implements Runnable {

    private static final int PERIOD = 5000;
    private final int DEST_PORT;
    private final String HOSTNAME;
    private NetworkInterface network;
    private byte[] mac;
    private InetAddress broadcastIP;
    private DatagramSocket socket;

    public Broadcaster(int DEST_PORT, String HOSTNAME) {
        this.DEST_PORT = DEST_PORT;
        this.HOSTNAME = HOSTNAME;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (network.getHardwareAddress() != null) {
                    network.getInterfaceAddresses().forEach(address -> {
                        if (address.getBroadcast() != null) {
                            this.network = network;
                            this.broadcastIP = address.getBroadcast();
                        }
                    });
                }
            }
            mac = network.getHardwareAddress();
        } catch (SocketException e) {
            System.err.println("Error while opening the socket");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void broadcastPacket() throws IOException {
        Packet packet = new Packet(mac, HOSTNAME);
        byte[] data = packet.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, broadcastIP, DEST_PORT);
        socket.send(datagramPacket);
    }

    @Override
    public void run() {
        try {
            while (true) {
                broadcastPacket();
                Thread.sleep(PERIOD);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted!");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error while broadcasting");
            System.exit(1);
        }
    }

}
