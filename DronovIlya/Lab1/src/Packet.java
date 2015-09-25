import exceptions.ProtoException;
import utils.Bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Packet {

    private static final int MAX_ADDRESS_LENGTH = 6;

    private final byte[] macAddress;
    private final byte hostnameLength;
    private final byte[] hostname;
    private final byte[] timestamp;

    public Packet(byte[] macAddress, byte hostnameLength, byte[] hostname, byte[] timestamp) {
        this.macAddress = macAddress;
        this.hostnameLength = hostnameLength;
        this.hostname = hostname;
        this.timestamp = timestamp;
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(macAddress.length + 1 + hostname.length + timestamp.length);
        buffer.put(macAddress);
        buffer.put(hostnameLength);
        buffer.put(hostname);
        buffer.put(timestamp);
        return buffer.array();
    }

    public String getMacAddress() {
        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddress) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String getName() {
        return new String(hostname, Charset.forName("UTF-8"));
    }

    public long getTimestamp() {
        ByteBuffer buffer = ByteBuffer.wrap(timestamp);
        return buffer.getLong();
    }

    public static Packet newInstance(byte[] macAddress, String hostname, long timestamp) {
        if (macAddress == null || hostname == null) {
            throw new IllegalArgumentException("macAddress and hostname must be non null");
        }
        byte[] name = hostname.getBytes(Charset.forName("UTF-8"));
        if (name.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("hostname must occupy only 1 byte");
        }
        byte nameLength = (byte) name.length;

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(timestamp);

        return new Packet(macAddress, nameLength, name, buffer.array());
    }

    public static Packet newInstance(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] macAddress = Bytes.readByteArray(buffer, MAX_ADDRESS_LENGTH);
        byte length = Bytes.readByte(buffer);
        byte[] hostname = Bytes.readByteArray(buffer, length);
        byte[] timestamp = Bytes.readByteArray(buffer, 8);

        return new Packet(macAddress, length, hostname, timestamp);
    }

}
