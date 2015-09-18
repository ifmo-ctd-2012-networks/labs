package lab1;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Message {
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
        byte[] timeStampBytes = new byte[Long.BYTES];
        System.arraycopy(message, message[6] + 7, timeStampBytes, 0, Long.BYTES);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(timeStampBytes);
        buffer.flip();
        timeStamp = buffer.getLong();
    }

    public byte[] getBytes() throws UnsupportedEncodingException {
        byte[] hostNameBytes = hostName.getBytes("UTF-8");
        byte[] message = new byte[hostNameBytes.length + Long.BYTES + 7];
        System.arraycopy(mac, 0, message, 0, 6);
        message[6] = (byte) hostNameBytes.length;
        System.arraycopy(hostNameBytes, 0, message, 7, hostNameBytes.length);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(timeStamp);
        System.arraycopy(buffer.array(), 0, message, message.length - Long.BYTES, Long.BYTES);
        return message;
    }
}
