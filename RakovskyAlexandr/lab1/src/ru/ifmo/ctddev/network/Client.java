package ru.ifmo.ctddev.network;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Client implements Runnable {

    public static final int MAC_LENGTH = 6;
    public static final int HOSTNAME_L = 1;
    public static final int TIMESTAMP_LENGTH = 8;

    public byte[] mac_address = new byte[MAC_LENGTH];
    public byte[] hostname_length = new byte[HOSTNAME_L];
    public byte[] hostname;
    public byte[] timestamp = new byte[TIMESTAMP_LENGTH];

    public void init() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            hostname = host.getBytes();
            hostname_length[0] = (byte)hostname.length;
            mac_address = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            Long time = System.currentTimeMillis() / 1000;
            timestamp = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(time).array();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public class ClientTask extends TimerTask {
        private byte[] data;

        public ClientTask() {
            data = new byte[MAC_LENGTH + HOSTNAME_L + hostname.length + TIMESTAMP_LENGTH];

            System.arraycopy(mac_address, 0, data, 0, MAC_LENGTH);
            System.arraycopy(hostname_length, 0, data, MAC_LENGTH, HOSTNAME_L);
            System.arraycopy(hostname, 0, data, MAC_LENGTH + HOSTNAME_L, hostname.length);
            System.arraycopy(timestamp, 0, data, MAC_LENGTH + HOSTNAME_L + hostname.length, TIMESTAMP_LENGTH);

        }

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket();
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface network = interfaces.nextElement();
                    byte[] ha = network.getHardwareAddress();
                    if (ha != null) {
                        List<InterfaceAddress> addresses = network.getInterfaceAddresses();
                        for (InterfaceAddress ia : addresses) {
                            if (ia.getBroadcast() != null) {
                                DatagramPacket packet = new DatagramPacket(data, data.length, ia.getBroadcast(), 6969);
                                socket.send(packet);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        init();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ClientTask(), 0, 5000);
    }
}
