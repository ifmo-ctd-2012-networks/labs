package ru.zyulyaev.ifmo.net.lab1;

import java.net.DatagramPacket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author zyulyaev
 */
public class UdpMessage {
    private final byte[] mac;
    private final String hostname;
    private final int timestamp;

    public UdpMessage(byte[] mac, String hostname, int timestamp) {
        this.mac = mac;
        this.hostname = hostname;
        this.timestamp = timestamp;
    }

    public DatagramPacket toPacket() {
        byte[] hostnameBytes = hostname.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer = ByteBuffer.allocate(6 + 1 + hostnameBytes.length + 4);
        buffer.put(mac)
                .put((byte) hostnameBytes.length)
                .put(hostnameBytes)
                .putInt(timestamp);
        byte[] data = buffer.array();
        return new DatagramPacket(data, data.length);
    }

    public static UdpMessage fromPacket(DatagramPacket packet) throws MessageMalformed {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
            byte[] mac = new byte[6];
            buffer.get(mac);
            int hostnameLength = Byte.toUnsignedInt(buffer.get());
            byte[] hostnameBytes = new byte[hostnameLength];
            buffer.get(hostnameBytes);
            int timestamp = buffer.getInt();
            return new UdpMessage(mac, new String(hostnameBytes, Charset.forName("UTF-8")), timestamp);
        } catch (BufferUnderflowException ex) {
            throw new MessageMalformed(ex);
        }
    }

    public byte[] getMac() {
        return mac;
    }

    public String getHostname() {
        return hostname;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getMacHex() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            if (i != 0)
                builder.append(':');
            builder.append(Integer.toHexString(Byte.toUnsignedInt(mac[i])));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "UdpMessage{" +
                "mac=" + getMacHex() +
                ", hostname='" + hostname + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
