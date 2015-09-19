package lab1;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Message {
    public static boolean TSL = false;
    public final byte[] mac;
    public final String hostName;
    public long timeStamp;

    public Message(byte[] mac, String hostName, long timeStamp) {
        this.mac = mac;
        this.hostName = hostName;
        this.timeStamp = timeStamp;
    }

    public Message(byte[] message) throws UnsupportedEncodingException {
        mac = new byte[6];
        System.arraycopy(message, 0, mac, 0, 6);
        int len = (((int) message[6]) + 256) % 256;
        byte[] hostNameBytes = new byte[len];
        System.arraycopy(message, 7, hostNameBytes, 0, len);
        hostName = new String(hostNameBytes, "UTF-8");
        byte[] timeStampBytes = new byte[TSL ? 8 : 4];
        System.arraycopy(message, message[6] + 7, timeStampBytes, 0, TSL ? 8 : 4);
        ByteBuffer buffer = ByteBuffer.allocate(TSL ? 8 : 4);
        buffer.put(timeStampBytes);
        buffer.flip();
        timeStamp = TSL ? buffer.getLong() : buffer.getInt();
    }

    public byte[] getBytes() throws UnsupportedEncodingException {
        byte[] hostNameBytes = hostName.getBytes("UTF-8");
        byte[] message = new byte[hostNameBytes.length + (TSL ? 15 : 11)];
        System.arraycopy(mac, 0, message, 0, 6);
        message[6] = (byte) hostNameBytes.length;
        System.arraycopy(hostNameBytes, 0, message, 7, hostNameBytes.length);
        ByteBuffer buffer = ByteBuffer.allocate(TSL ? 8 : 4);
        if (TSL)
            buffer.putLong(timeStamp);
        else
            buffer.putInt((int) timeStamp);
        int l = TSL ? 8 : 4;
        System.arraycopy(buffer.array(), 0, message, message.length - l, l);
        return message;
    }
}
