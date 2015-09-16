package ru.ifmo.info;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

public class Message implements Comparable<Message> {
    private static final NodeInfo localInfo = NodeInfo.makeLocal();

    private final NodeInfo info;
    private final long timestamp;

    private Message(NodeInfo info, long timestamp) {
        this.info = info;
        this.timestamp = timestamp;
    }

    Message(NodeInfo info) {
        this(info, System.currentTimeMillis());
    }

    public Message(byte[] bytes) {
        MacAddress mac = new MacAddress(Arrays.copyOf(bytes, MacAddress.SIZE));

        byte hostnameSize = bytes[MacAddress.SIZE];
        String hostname = new String(bytes, MacAddress.SIZE + 1, hostnameSize, Charset.forName("UTF-8"));
        info = new NodeInfo(mac, hostname);

        timestamp = bytesToLong(bytes, MacAddress.SIZE + 1 + hostnameSize);
    }

    public NodeInfo getNodeInfo() {
        return info;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Message makeLocal() {
        return localInfo.toMessage();
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(info.getMacAdress().value);
            bos.write((byte) info.getHostname().length());
            bos.write(info.getHostname().getBytes(Charset.forName("UTF-8")));
            bos.write(longToBytes(timestamp));
            return bos.toByteArray();
        } catch (IOException e) {
            // ignore, actually never throws
            throw new Error("Something strange happened");
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "mac=" + info.getMacAdress() +
                ", hostname=" + info.getHostname() +
                ", timestamp=" + new Date(timestamp) +
                '}';
    }

    private static long bytesToLong(byte[] bytes, int offset) {
        long res = 0;
        int size = Long.SIZE / Byte.SIZE;
        int byteUnsignedMaxValue = 1 << Byte.SIZE;

        for (int i = 0; i < size; i++) {
            res = (res << 8) + ((long) bytes[i + offset] + byteUnsignedMaxValue) % byteUnsignedMaxValue;
        }
        return res;
    }

    private static byte[] longToBytes(long v) {
        byte[] res = new byte[Long.SIZE / Byte.SIZE];
        for (int i = res.length - 1; i >= 0; i--) {
            res[i] = (byte) v;
            v >>>= 8;
        }
        return res;
    }

    @Override
    public int compareTo(Message o) {
        return info.compareTo(o.info);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return info.equals(message.info);

    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }
}
