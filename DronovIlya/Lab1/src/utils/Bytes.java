package utils;

import exceptions.ProtoException;

import java.nio.ByteBuffer;

public class Bytes {

    public static byte[] readByteArray(ByteBuffer buffer, int size) throws ProtoException {
        checkBufferSize(buffer, size);
        byte[] result = new byte[size];
        buffer.get(result, 0, size);
        return result;
    }

    public static byte readByte(ByteBuffer buffer) throws ProtoException {
        checkBufferSize(buffer, 1);
        return buffer.get();
    }

    private static void checkBufferSize(ByteBuffer buffer, int size) throws ProtoException {
        if (buffer.remaining() < size || size < 0) {
            throw new ProtoException();
        }
    }
}
