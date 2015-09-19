package network;

import java.nio.ByteBuffer;

/**
 * Created by kerzo on 19.09.2015.
 */
public class Utils {
    public static final int HOSTNAME_LENGTH_OFFSET = 1;
    public static final int MAC_OFFSET = 6;
    public static final int HOSTNAME_OFFSET = MAC_OFFSET + HOSTNAME_LENGTH_OFFSET;

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public static byte[] getTimestamp() {
        return longToBytes(System.currentTimeMillis() / 1000L);
    }

    public static String fitToWidth(String s, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.substring(0, Math.min(width, s.length())));
        width -= s.length();
        for (; width > 0; width--) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
