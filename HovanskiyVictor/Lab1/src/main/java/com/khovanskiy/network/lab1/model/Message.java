package com.khovanskiy.network.lab1.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author victor
 */
public class Message {

    private final MacAddress mac;
    private final String hostname;
    private final long timestamp;

    public Message(MacAddress mac, String hostname, long timestamp) {
        this.mac = mac;
        this.hostname = hostname;
        this.timestamp = timestamp;
    }

    public Message(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] mac = new byte[6];
        buffer = buffer.get(mac, 0, 6);
        this.mac = new MacAddress(mac);

        int hostnameLength = buffer.get();
        byte[] hostname = new byte[hostnameLength];
        buffer = buffer.get(hostname, 0, hostnameLength);
        this.hostname = new String(hostname, StandardCharsets.UTF_8);

        this.timestamp = buffer.getLong();
    }

    public byte[] getBytes() {
        byte[] mac = this.mac.getBytes();
        assert hostname.length() < 256;
        byte length = (byte) hostname.length();
        byte[] bytes = hostname.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(mac.length + 1 + bytes.length + Long.BYTES);
        buffer.put(mac);
        buffer.put(length);
        buffer.put(bytes);
        buffer.putLong(timestamp);
        return buffer.array();
    }

    @Override
    public String toString() {
        return "Message[mac=" + mac + ", hostname=" + hostname + ", timestamp=" + timestamp + "]";
    }
}
