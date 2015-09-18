package ru.ifmo.util;

public class PrimitiveDataConverter {
    public static long bytesToLong(byte[] bytes, int offset) {
        long res = 0;
        int size = Long.SIZE / Byte.SIZE;
        int byteUnsignedMaxValue = 1 << Byte.SIZE;

        for (int i = 0; i < size; i++) {
            res = (res << 8) + ((long) bytes[i + offset] + byteUnsignedMaxValue) % byteUnsignedMaxValue;
        }
        return res;
    }

    public static byte[] longToBytes(long v) {
        byte[] res = new byte[Long.SIZE / Byte.SIZE];
        for (int i = res.length - 1; i >= 0; i--) {
            res[i] = (byte) v;
            v >>>= 8;
        }
        return res;
    }

}
