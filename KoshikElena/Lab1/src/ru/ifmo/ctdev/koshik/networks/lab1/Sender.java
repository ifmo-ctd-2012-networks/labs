package ru.ifmo.ctdev.koshik.networks.lab1;


import java.net.*;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Sender implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(Sender.class.getName());

    private DatagramSocket socket;

    private final int port;

    private final long period;

    private NetworkInterface networkInterface;

    public Sender(int port, int period, NetworkInterface ni) throws SocketException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        LOGGER.addHandler(new ConsoleHandler());
        LOGGER.setUseParentHandlers(false);

        this.port = port;
        this.period = period;
        networkInterface = ni;
    }

    @Override
    public void run() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            byte[] mac = networkInterface.getHardwareAddress();

            String hostname = InetAddress.getLocalHost().getHostName();
            InetAddress broadcastAddress = getBroadcastAddress(networkInterface);

            while (!Thread.currentThread().isInterrupted()) {
                long unixTime = System.currentTimeMillis() / 1000l;

                byte[] buf = Packet.createPacket(mac, hostname, unixTime);

                DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, port);
                socket.send(packet);

                LOGGER.info("Packet has been SENT" + "\n" +
                        "       MAC address: " + Packet.getMacAddress(mac) + "\n" +
                        "       Hostname: " + hostname + "\n" +
                        "       Unix Timestamp: " + unixTime + "\n");
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private InetAddress getBroadcastAddress(NetworkInterface ni) {
        for (InterfaceAddress address : ni.getInterfaceAddresses()) {
            InetAddress broadcast = address.getBroadcast();
            if (broadcast != null)
                return broadcast;
        }
        return null;
    }
}
