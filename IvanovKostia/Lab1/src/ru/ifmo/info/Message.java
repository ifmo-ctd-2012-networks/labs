package ru.ifmo.info;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
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

    public Message(byte[] bytes) throws MessageParseException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes)
                    .order(ByteOrder.BIG_ENDIAN);

            byte[] macBytes = new byte[MacAddress.SIZE];
            buffer.get(macBytes);
            MacAddress mac = new MacAddress(macBytes);

            byte hostnameSize = buffer.get();
            byte[] hostnameBytes = new byte[hostnameSize];
            buffer.get(hostnameBytes);
            String hostname = new String(hostnameBytes, Charset.forName("UTF-8"));
            info = new NodeInfo(mac, hostname);

            timestamp = buffer.getLong() * 1000;
        } catch (Exception e) {
            throw new MessageParseException();
        }
    }

    public NodeInfo getNodeInfo() {
        return info;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] toBytes() {
        return ByteBuffer.allocate(MacAddress.SIZE + 1 + info.getHostname().length() + Long.BYTES / Byte.BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .put(info.getMacAdress().value)
                .put((byte) info.getHostname().length())
                .put(info.getHostname().getBytes(Charset.forName("UTF-8")))
                .putLong((timestamp / 1000))
                .array();
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
