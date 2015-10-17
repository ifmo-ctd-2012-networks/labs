package ru.ifmo.ctddev.varlamov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CrashServer implements Runnable {

    private static final int PERIOD = 5000;
    private static final int HOSTNAME_LENGTH = 100;

    private List<SocketHolder> socketHolders = new ArrayList<>();
    private final int port;
    private final Random r = new Random(System.currentTimeMillis());

    private String generateString(int codes, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = (char)r.nextInt(codes);
        }
        return new String(text);
    }

    private class SocketHolder {
        private DatagramSocket socket;
        private InetAddress inetAddress;
        private byte[] hardwareAddress;

        private SocketHolder(NetworkInterface ni, InetAddress inetAddress) throws SocketException {
            this.hardwareAddress = ni.getHardwareAddress();
            this.inetAddress = inetAddress;
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        }

        private DatagramPacket getPacket() throws SocketException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(hardwareAddress, 0, 6);

            String hostname = generateString(64, HOSTNAME_LENGTH);
            byte[] hbytes = Charset.forName("UTF-8").encode(hostname).array();
            byte[] length = new byte[]{(byte) hbytes.length};
            os.write(length, 0, 1);
            os.write(hbytes, 0, hbytes.length);
            long unixTime = (System.currentTimeMillis() / 1000L);
            byte[] unixBytes = new byte[]{
                    (byte) (unixTime >> 24),
                    (byte) (unixTime >> 16),
                    (byte) (unixTime >> 8),
                    (byte) unixTime
            };
            /*ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(unixTime);
            byte[] unixBytes = buffer.array();/**/
            os.write(unixBytes, 0, unixBytes.length);
            byte[] bytes = os.toByteArray();
            return new DatagramPacket(bytes, bytes.length, new InetSocketAddress(inetAddress, port));
        }
    }

    public CrashServer(int port) {
        this.port = port;
        try {
            for (Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces(); eni.hasMoreElements(); ) {
                NetworkInterface ni = eni.nextElement();
                if (!ni.isLoopback() && ni.isUp() && ni.supportsMulticast()) {
                    for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                        InetAddress inetAddress = interfaceAddress.getBroadcast();
                        if (inetAddress != null) {
                            socketHolders.add(new SocketHolder(ni, inetAddress));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (SocketHolder socketHolder : socketHolders) {
                    try {
                        socketHolder.socket.send(socketHolder.getPacket());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, PERIOD);
    }
}