package ru.ifmo.info;

import ru.ifmo.util.PrimitiveDataConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

public class Message implements Comparable<Message> {
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

        timestamp = PrimitiveDataConverter.bytesToLong(bytes, MacAddress.SIZE + 1 + hostnameSize) * 1000;
    }

    public NodeInfo getNodeInfo() {
        return info;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(info.getMacAdress().value);
            bos.write((byte) info.getHostname().length());
            bos.write(info.getHostname().getBytes(Charset.forName("UTF-8")));
            bos.write(PrimitiveDataConverter.longToBytes(timestamp / 1000));
            return bos.toByteArray();
        } catch (IOException e) {
            // actually never throws
            throw new Error("Something strange (exception is not expected here)");
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", info, new Date(timestamp));
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
