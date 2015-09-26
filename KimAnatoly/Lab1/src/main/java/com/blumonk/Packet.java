package com.blumonk;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author akim
 * MAC ADDRESS, (6 bytes BE)
 * Hostname string length, (1 byte)
 * Hostname, (UTF-8 string)
 * UNIX Timestamp (long)
 */
public class Packet {

    private byte[] mac;
    private byte length;
    private byte[] hostname;
    private byte[] timestamp;
    private static final String ENCODING = "UTF-8";

    public Packet(byte[] mac, String hostname) {
        this.mac = mac;
        try {
            this.hostname = hostname.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        timestamp = Utils.longToBytes(System.currentTimeMillis() / 1000L);
        length = (byte) hostname.length();
    }

    public Packet(byte[] bytes) {
        if (bytes.length < 11) {
            throw new IllegalArgumentException("The packet is too short");
        }
        mac = Arrays.copyOfRange(bytes, 0, 6);
        length = bytes[6];
        if (length < 0 || length > bytes.length - 7) {
            throw new IllegalArgumentException("Invalid length of the hostname");
        }
        hostname = Arrays.copyOfRange(bytes, 7, 7 + length);
        int tsLength = bytes.length - 7 - length;
        if (tsLength == 4 || tsLength == 8) {
            timestamp = Arrays.copyOfRange(bytes, 7 + length, bytes.length);
        } else {
            throw new IllegalArgumentException("Excess bytes");
        }
    }

    public byte[] getBytes() {
        int offset = 0;
        byte[] result = new byte[7 + hostname.length + timestamp.length];
        System.arraycopy(mac, 0, result, 0, mac.length);
        offset = mac.length;
        result[offset] = length;
        offset++;
        System.arraycopy(hostname, 0, result, offset, hostname.length);
        offset += hostname.length;
        System.arraycopy(timestamp, 0, result, offset, timestamp.length);
        return result;
    }

    public byte getLength() {
        return length;
    }

    public String getMac() {
        return Utils.macToString(mac);
    }

    public String getHostname() {
        return Utils.bytesToString(hostname);
    }

    public long getTimestamp() {
        return Utils.bytesToLong(timestamp);
    }
}
