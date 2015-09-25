package utils;

import exceptions.ProtoException;

import java.nio.ByteBuffer;

public class Bytes {

    public static byte[] readByteArray(ByteBuffer buffer, int size) {
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
