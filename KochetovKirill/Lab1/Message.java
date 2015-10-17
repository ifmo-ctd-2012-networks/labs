
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;

public class Message {
    public final byte[] mac;
    public final String hostName;
    public long timeStamp;
    private int timeStampLen = 8;

    public Message(byte[] mac, String hostName) {
        this.mac = mac;
        this.hostName = hostName;
    }

    public Message(byte[] message) throws UnsupportedEncodingException, ParseException {
        mac = new byte[6];
//        if (message.length < 6) {
//            System.out.println("1");
//            throw new ParseException("Received packet hasn`t enough count of bytes for mac address");
//
//        }
        System.arraycopy(message, 0, mac, 0, 6);
//        if (message.length < 7) {
//            System.out.println("2");
//            throw new ParseException("Received packet hasn`t enough count of bytes for host name length");
//        }
        int len = (((int) message[6]) + 256) % 256;
        byte[] hostNameBytes = new byte[len];

//        if (message.length < 7 + len) {
//            System.out.println("3");
//            throw new ParseException("Received packet hasn`t enough count of bytes for host name");
//        }
        System.arraycopy(message, 7, hostNameBytes, 0, len);
        hostName = new String(hostNameBytes, "UTF-8");
        byte[] timeStampBytes = new byte[timeStampLen];
//        if (message.length < 11 + len) {
//            System.out.println("4");
//            throw new ParseException("Received packet hasn`t enough count of bytes for timestamp");
//        }
        if (message[6] < 0) {
            System.out.println("Negative hostName length");
            return;
        }
        System.arraycopy(message, message[6] + 7, timeStampBytes, 0, timeStampLen);
        ByteBuffer buffer = ByteBuffer.allocate(timeStampLen);
        buffer.put(timeStampBytes);
        buffer.flip();
        if (timeStampLen != 4) {
            timeStamp = buffer.getLong();
        } else {
            timeStamp = buffer.getInt();
        }

//        System.out.println("Received packet from ----- " + hostName);
    }



    public byte[] getBytes() throws UnsupportedEncodingException {
        byte[] hostNameBytes = hostName.getBytes("UTF-8");
        byte[] message = new byte[hostNameBytes.length + (timeStampLen + 7)];
        System.arraycopy(mac, 0, message, 0, 6);
        message[6] = (byte) hostNameBytes.length;
        System.arraycopy(hostNameBytes, 0, message, 7, hostNameBytes.length);
        ByteBuffer buffer = ByteBuffer.allocate(timeStampLen);
        if (timeStampLen != 4)
            buffer.putLong(timeStamp);
        else
            buffer.putInt((int) timeStamp);
        int l = timeStampLen;
        System.arraycopy(buffer.array(), 0, message, message.length - l, l);
        return message;
//        Random rand = new Random();
//        byte[] message = new byte[7];
//        rand.nextBytes(message);
//        return message;
    }
}
