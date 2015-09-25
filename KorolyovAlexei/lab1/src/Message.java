package src;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Message {
    private byte[] host;
    private byte[] mac;
    private long timestamp;

    private String macAsString;
    private String hostAsString;
    private byte[] asByteArray;

    public Message(byte[] host, byte[] mac, long timestamp) {
        this.host = host;
        this.mac = mac;
        this.timestamp = timestamp;
    }

    public static Message fromByteArray(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byte[] mac = new byte[6];
        byteBuffer = byteBuffer.get(mac, 0, 6);
        byte hostLength = byteBuffer.get();
        byte[] host = new byte[hostLength];
        byteBuffer = byteBuffer.get(host, 0, hostLength);
        long timestamp = byteBuffer.getLong();
        return new Message(host, mac, timestamp);
    }

    public String getHost() {
        if (hostAsString == null) {
            try {
                hostAsString = new String(host, "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return hostAsString;
    }

    public String getMac() {
        if (macAsString == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macAsString = sb.toString();
        }
        return macAsString;
    }

    public byte[] toByteArray() {
        if (asByteArray == null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(7 + host.length + (Long.SIZE / Byte.SIZE));
            byteBuffer.put(mac);
            byteBuffer.put((byte) host.length);
            byteBuffer.put(host);
            byteBuffer.putLong(timestamp);
            asByteArray = byteBuffer.array();
        }
        return asByteArray;
    }

    public int length() {
        if (asByteArray == null) {
            toByteArray();
        }
        return asByteArray.length;
    }

    @Override
    public String toString() {
        if (hostAsString == null) {
            getHost();
        }
        if (macAsString == null) {
            getMac();
        }
        return String.format("Host: %s, MAC: %s, Timestamp: %s", hostAsString, macAsString, timestamp);
    }
}
