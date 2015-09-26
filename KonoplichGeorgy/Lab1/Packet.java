import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Created by Георгий on 26.09.2015.
 */
public class Packet {
    private static final int MAX_ADDRESS_LENGTH = 6;

    private final byte[] macAddress;
    private final byte nameLength;
    private final byte[] name;
    private final byte[] timestamp;

    public Packet(byte[] macAddress, byte nameLength, byte[] name, byte[] timestamp) {
        this.macAddress = macAddress;
        this.nameLength = nameLength;
        this.name = name;
        this.timestamp = timestamp;
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

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(macAddress.length + 1 + name.length + timestamp.length);
        buffer.put(macAddress);
        buffer.put(nameLength);
        buffer.put(name);
        buffer.put(timestamp);
        return buffer.array();
    }

    public String getName() {
        return new String(name, Charset.forName("UTF-8"));
    }

    public long getTimestamp() {
        ByteBuffer buffer = ByteBuffer.wrap(timestamp);
        return buffer.getLong();
    }

    public static Packet newInstance(byte[] macAddress, String name, long timestamp) {
        if (macAddress == null || name == null) {
            throw new IllegalArgumentException("macAddress and name must be non null");
        }
        byte[] name1 = name.getBytes(Charset.forName("UTF-8"));
        if (name1.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("name must occupy only 1 byte");
        }
        byte nameLength = (byte) name1.length;

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(timestamp);

        return new Packet(macAddress, nameLength, name1, buffer.array());
    }

    public static Packet getInstance(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] macAddress = Util.readByteArray(buffer, MAX_ADDRESS_LENGTH);
        byte length = Util.readByte(buffer);
        byte[] name = Util.readByteArray(buffer, length);
        byte[] timestamp = Util.readByteArray(buffer, 8);

        return new Packet(macAddress, length, name, timestamp);
    }
    public static class Util {

        public static byte[] readByteArray(ByteBuffer buffer, int size) {
            if (size < 0) size = 0;
            checkBufferSize(buffer, size);
            byte[] result = new byte[size];
            buffer.get(result, 0, size);
            return result;
        }

        public static byte readByte(ByteBuffer buffer) {
            checkBufferSize(buffer, 1);
            return buffer.get();
        }

        private static void checkBufferSize(ByteBuffer buffer, int size) {
            if (buffer.remaining() < size) {
                throw new ProtoException();
            }
        }

    }
    public static class ProtoException extends RuntimeException {

        public ProtoException() {
            super("Received invalid packet");
        }
    }
}
