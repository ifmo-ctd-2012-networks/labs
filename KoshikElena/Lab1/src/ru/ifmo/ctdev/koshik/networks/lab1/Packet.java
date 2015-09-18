package ru.ifmo.ctdev.koshik.networks.lab1;


import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Packet {

    public static final int MAC_SIZE = 6;

    public final String macAddress;
    public final String hostname;
    public final byte hostnameLength;
    public final long timestamp;


    public Packet(DatagramPacket packet) {
        byte[] data = packet.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        byte[] mac = new byte[6];
        byteBuffer.get(mac);

        macAddress = getMacAddress(mac);

        hostnameLength = byteBuffer.get();

        byte[] nameBuf = new byte[hostnameLength];
        byteBuffer.get(nameBuf);
        hostname = new String(nameBuf);

        timestamp = byteBuffer.getLong();
    }

    public static byte[] createPacket(byte[] macAddress, String hostname, long timestamp) {
        byte[] hostnameBuf = hostname.getBytes();

        byte hostnameLength = (byte)hostnameBuf.length;

        int bufSize = Packet.MAC_SIZE + 1 + hostnameLength + 8;

        return ByteBuffer.allocate(bufSize)
                .put(macAddress).put(hostnameLength).put(hostnameBuf).putLong(timestamp).array();
    }

    public static String getMacAddress(byte[] address) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < address.length; i++) {
            sb.append(String.format("%02x%s", address[i], (i < address.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

}
