package hw1;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public class ReceivedInfo implements Comparable {
    private static final Logger log = Logger.getLogger(ReceivedInfo.class.getName());

    private String macAddress;
    private String host;
    private Date time;
    private boolean isIncorrectData;
    private int counter;

    public ReceivedInfo(byte[] packet) {
        isIncorrectData = false;
        if (packet.length < 7) { // 7 = macAddress + hostLength
            isIncorrectData = true;
            log.severe("packet length = " + packet.length);
            return;
        }
        byte[] macAddressBytes = Arrays.copyOfRange(packet, 0, 6);
        byte hostLength = Arrays.copyOfRange(packet, 6, 7)[0];
        if (hostLength < 0) {
            isIncorrectData = true;
            log.severe("hostLength = " + hostLength + " < 0");
            return;
        }
        byte[] hostBytes = Arrays.copyOfRange(packet, 7, Math.min(7 + hostLength, packet.length));
        if (Math.min(7 + hostLength, packet.length) == packet.length || 7 + 4 + hostLength >= packet.length) {
            isIncorrectData = true;
            log.severe("no timestamp, packet from sever too short");
            return;
        }
        byte[] timeBytes = Arrays.copyOfRange(packet, 7 + hostLength, 7 + 4 + hostLength);

        macAddress = "";
        for (byte macAddressByte : macAddressBytes) {
            macAddress += String.format("-%02X", macAddressByte);
        }
        macAddress = macAddress.substring(1);
        host = new String(hostBytes, Charset.forName("UTF-8"));
        time = new Date((long) intByBytes(timeBytes) * 1000);
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

    public boolean isIncorrectData() {
        return isIncorrectData;
    }

    private int intByBytes(byte[] bytes) {
        return bytes[3] & 0xFF |
                (bytes[2] & 0xFF) << 8 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[0] & 0xFF) << 24;
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
