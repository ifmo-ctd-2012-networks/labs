package hw1;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

public class ReceivedInfo implements Comparable {
    private String macAddress;
    private String host;
    private Date time;
    private int counter;

    public ReceivedInfo(byte[] packet) {
        byte[] macAddressBytes = Arrays.copyOfRange(packet, 0, 6);
        byte hostLength = Arrays.copyOfRange(packet, 6, 7)[0];
        byte[] hostBytes = Arrays.copyOfRange(packet, 7, 7 + hostLength);
        byte[] timeBytes = Arrays.copyOfRange(packet, 7 + hostLength, packet.length - (7 + hostLength));
        macAddress = "";
        for (byte macAddressByte : macAddressBytes) {
            macAddress += String.format("-%02X", macAddressByte);
        }
        macAddress = macAddress.substring(1);
        host = new String(hostBytes, Charset.defaultCharset());
        time = new Date(longByBytes(timeBytes));
        counter = 0;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getHost() {
        return host;
    }

    public Date getTime() {
        return time;
    }

    public int getCounter() {
        return counter;
    }

    public void incCounter() {
        ++counter;
    }

    private long longByBytes(byte[] bytes) {
        return ((bytes[7] & 0xFFL)) +
                ((bytes[6] & 0xFFL) << 8) +
                ((bytes[5] & 0xFFL) << 16) +
                ((bytes[4] & 0xFFL) << 24) +
                ((bytes[3] & 0xFFL) << 32) +
                ((bytes[2] & 0xFFL) << 40) +
                ((bytes[1] & 0xFFL) << 48) +
                (((long) bytes[0]) << 56);
    }

    @Override
    public String toString() {
        return macAddress + " " + host + " " + time.toString() + " " + counter;
    }

    @Override
    public int compareTo(Object o) {
        ReceivedInfo info = (ReceivedInfo) o;
        return macAddress.compareTo(info.getMacAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReceivedInfo info = (ReceivedInfo) o;

        return macAddress.equals(info.macAddress);

    }

    @Override
    public int hashCode() {
        return macAddress.hashCode();
    }
}
