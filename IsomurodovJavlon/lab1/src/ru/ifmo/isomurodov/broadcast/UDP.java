package ru.ifmo.isomurodov.broadcast;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by javlon on 18.09.15.
 */
public class UDP {
    private byte[] macAdress;
    private byte hostnameLength;
    private byte[] hostname;
    private byte[] timeStamp;

    public UDP(byte[] macAdress, byte[] hostname, byte[] timeStamp) {
        this.macAdress = Arrays.copyOfRange(macAdress, 0, macAdress.length);
        this.hostname = Arrays.copyOfRange(hostname, 0, hostname.length);
        if (this.hostname.length > 255) {
            throw new IllegalArgumentException("Bad hostname!");
        }
        this.hostnameLength = (byte) this.hostname.length;
        this.timeStamp = Arrays.copyOfRange(timeStamp, 0, timeStamp.length);
    }

    public UDP(byte[] bytes) {
        macAdress = Arrays.copyOfRange(bytes, 0, 6);
        hostnameLength = bytes[6];
        hostname = Arrays.copyOfRange(bytes, 7, 7 + (int) hostnameLength);
        timeStamp = Arrays.copyOfRange(bytes, 7 + (int) hostnameLength, bytes.length);
    }

    public byte[] getBytes() {
        byte[] result = new byte[11 + hostname.length];
        System.arraycopy(macAdress, 0, result, 0, 6);
        result[6] = hostnameLength;
        System.arraycopy(hostname, 0, result, 7, hostname.length);
        System.arraycopy(timeStamp, 0, result, 7 + hostname.length, 4);
        return result;
    }

    public byte[] getMacAdress() {
        return Arrays.copyOfRange(macAdress, 0, 6);
    }

    public String getHostname() {
        return new String(hostname, Charset.forName("UTF-8"));
    }

    public long getTimeStamp() {
        ByteBuffer buf = ByteBuffer.wrap(timeStamp);
        buf.order(ByteOrder.BIG_ENDIAN);
        int ts = buf.getInt();
        return ts & 0x00000000ffffffffL;
    }
}
