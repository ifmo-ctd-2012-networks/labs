package network;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by kerzo on 19.09.2015.
 */
public class Message {
    private String macAddress;
    private String hostName;
    private long timestamp;

    public Message(String macAddress, String hostName, long timestamp) {
        this.macAddress = macAddress;
        this.hostName = hostName;
        this.timestamp = timestamp;
    }

    public Message(byte[] buffer) {
        byte[] macBytes = Arrays.copyOfRange(buffer, 0, Utils.MAC_OFFSET);
        macAddress = bytesToMACAddress(macBytes);

        byte hostNameLength = buffer[Utils.MAC_OFFSET];
        byte[] hostNameBytes = Arrays.copyOfRange(buffer, Utils.HOSTNAME_OFFSET, Utils.HOSTNAME_OFFSET + hostNameLength);
        hostName = new String(hostNameBytes);

        byte[] timestampBytes = Arrays.copyOfRange(buffer, Utils.HOSTNAME_OFFSET + hostNameLength, Utils.HOSTNAME_OFFSET + hostNameLength + 8);
        timestamp = Utils.bytesToLong(timestampBytes);
    }

    private static String bytesToMACAddress(byte[] macBytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macBytes.length; i++) {
            sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp(Long timestamp) {
        if (timestamp != null) {
            this.timestamp = timestamp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return macAddress.equals(message.macAddress);

    }

    @Override
    public int hashCode() {
        return macAddress.hashCode();
    }
}
