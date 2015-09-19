package hw1;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

public class Server implements Runnable {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    private final byte[] macAddress;

    public Server() {
        macAddress = getMacAddress();
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            try {
                socket.setBroadcast(true);
            } catch (SocketException e) {
                log.severe("Can't create broadcast socket, error message: " + e.getMessage());
                return;
            }
            log.info("Server is ready to send broadcast packets on port " + Main.port);
            while (!Thread.interrupted()) {
                Collections.list(NetworkInterface.getNetworkInterfaces())
                        .stream()
                        .filter(networkInterface -> {
                            try {
                                return !networkInterface.isLoopback() && networkInterface.isUp();
                            } catch (SocketException e) {
                                log.severe("Socket check error, error message: " + e.getMessage());
                                return false;
                            }
                        })
                        .map(NetworkInterface::getInterfaceAddresses)
                        .forEach(interfaceAddresses -> interfaceAddresses
                                .stream()
                                .filter(interfaceAddress -> interfaceAddress.getBroadcast() != null)
                                .forEach(interfaceAddress -> {
                                    InetAddress inetAddress = interfaceAddress.getBroadcast();
                                    byte[] data = buildSendData();
                                    if (data == null) {
                                        log.severe("Byte array is null");
                                    } else {
                                        DatagramPacket sendPacket = new DatagramPacket(data, data.length);
                                        sendPacket.setAddress(inetAddress);
                                        sendPacket.setPort(Main.port);
                                        try {
                                            socket.send(sendPacket);
                                        } catch (IOException e) {
                                            log.severe("Can't send packet with data, " + e.getMessage());
                                        }
                                    }
                                }));
                try {
                    Thread.sleep(Main.millisecondsSleepServer);
                } catch (InterruptedException e) {
                    log.severe("Thread sleep was interrupted, error message: " + e.getMessage());
                    return;
                }
            }
        } catch (SocketException e) {
            log.severe("Socket creating error, error message: " + e.getMessage());
        }
    }

    private byte[] buildSendData() {
        try {
            String hostAddress = "Pavel Asadchiy " + InetAddress.getLocalHost().getHostName();
            byte[] hostAddressBytes = hostAddress.getBytes(Charset.defaultCharset());
            byte[] hostAddressLengthBytes = new byte[1];
            hostAddressLengthBytes[0] = (byte) hostAddress.length();
            int currentTimeSeconds = (int) (System.currentTimeMillis() / 1000);
            byte[] timesTamp = intToByteArray(currentTimeSeconds);
            log.info("Data to send: " + getNormalMacAddress() + " " + hostAddress + " "
                    + hostAddress.length() + " " + new Date((long) currentTimeSeconds * 1000));
            return mergeByteArrays(mergeByteArrays(macAddress, hostAddressLengthBytes), mergeByteArrays(hostAddressBytes, timesTamp));
        } catch (UnknownHostException e) {
            log.severe("Can't get host, error message: " + e.getMessage());
        }
        return null;
    }

    private byte[] mergeByteArrays(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];
        System.arraycopy(one, 0, combined, 0         , one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);
        return combined;
    }

    private byte[] longToByteArray(long value) {
        return new byte[] {
                (byte) (value >> 56),
                (byte) (value >> 48),
                (byte) (value >> 40),
                (byte) (value >> 32),
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    private byte[] intToByteArray(long value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    private String getNormalMacAddress() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macAddress.length; i++) {
            sb.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

    public static byte[] getMacAddress() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    ip = addresses.nextElement().getHostAddress();
                }
            }
            InetAddress name = InetAddress.getByName(ip);
            NetworkInterface network = NetworkInterface.getByInetAddress(name);
            return network.getHardwareAddress();
        } catch (SocketException | UnknownHostException e) {
            log.severe("Can't get MAC address, error message: " + e.getMessage());
        }
        return null;
    }
}
