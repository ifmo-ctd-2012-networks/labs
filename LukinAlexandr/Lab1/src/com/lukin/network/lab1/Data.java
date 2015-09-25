package com.lukin.network.lab1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by Саша on 18.09.2015.
 */
public class Data {

    private final MACAddress macAddress;
    private final String hostname;
    private final long timestamp;

    public Data(MACAddress macAddress, String hostname, long timestamp) throws Exception {
        if (hostname.length() >= 256)
            throw new Exception("Hostname length > 256");
        this.macAddress = macAddress;
        this.hostname = hostname;
        this.timestamp = timestamp;
    }
    public static Data convertBytes (byte[] bytes) throws Exception {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byte[] address = new byte[6];
            byteBuffer = byteBuffer.get(address, 0, 6); //macAddress filled with bytes
            MACAddress macAddress = new MACAddress(address);
            int hostnameLength = byteBuffer.get();
            byte[] hostname = new byte[hostnameLength];
            byteBuffer = byteBuffer.get(hostname, 0, hostnameLength);
            String name = new String(hostname, StandardCharsets.UTF_8);
            long timestamp = byteBuffer.getLong();

            return new Data(macAddress, name, timestamp);
    }
    public byte[] getBytes(){
        byte[] address = this.macAddress.getBytes();
        byte length = (byte) hostname.length();
        byte[] bytes = hostname.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length + 15); //MAC address = 6 bytes, length = 1 byte, long = 8 bytes
        byteBuffer.put(address);
        byteBuffer.put(length);
        byteBuffer.put(bytes);
        byteBuffer.putLong(timestamp);
        return byteBuffer.array();
    }

    public MACAddress getMacAddress() {
        return macAddress;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return "Data{" +
                "macAddress=" + macAddress +
                ", hostname='" + hostname +
                ", timestamp=" + timestamp +
                '}';
    }
}
