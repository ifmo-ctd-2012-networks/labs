package com.blumonk;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author akim
 */
public class Utils {

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String macToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(18);
        for (byte b : bytes) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        longBuffer.putLong(x);
        return longBuffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        longBuffer.put(bytes);
        longBuffer.flip();
        return longBuffer.getLong();
    }
}
