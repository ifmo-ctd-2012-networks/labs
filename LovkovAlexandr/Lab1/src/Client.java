import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Charm on 15/09/15.
 */
public class Client implements Runnable {

    private int port;
    private int iterations;
    ConcurrentLinkedQueue <Node > queue;

    Client(int port, int iterations, ConcurrentLinkedQueue<Node> queue) {
        this.port = port;
        this.iterations = iterations;
        this.queue = queue;
        System.out.println("Client, port:" + port);
    }

    @Override
    public void run() {
        DatagramSocket c;
        try {
            c = new DatagramSocket(port);
            c.setBroadcast(true);
            int count = 0;
            while (true) {
                try {
                    count++;
                    if (iterations != 0 && count > iterations) {
                        break;
                    }
                    byte[] recvBuf = new byte[1000];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    c.setSoTimeout(5000);
                    c.receive(receivePacket);
                    byte[] message = receivePacket.getData();
                    parseResponse(message);
                } catch (IOException e) {
                    //System.out.println(e + " " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Client can't get DatagramSocket");
        }
    }

   private void parseResponse(byte[] message) throws UnsupportedEncodingException {
        message = trim(message);
        String macAddress = Main.getStringFromByteArray(extract(message, 0, 6));
        Byte hostLength = extract(message, 6, 1)[0];
        String host = new String(extract(message, 7, hostLength), "UTF-8");
        long timestamp = bytesToLong(extract(message, 7 + hostLength, message.length - (7 + hostLength)));
        Node node = new Node(macAddress, host, true, new Date(timestamp));
        queue.add(node);
    }

    public byte[] extract(byte[] data, int offset, int length) {
        byte[] ans = new byte[length];
        System.arraycopy(data, offset, ans, 0, length);
        return ans;
    }

    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        while (buffer.remaining() > 0) {
            buffer.put((byte) 0);
        }
        buffer.flip();
        return buffer.getLong();
    }

    static byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            i--;
        }
        return Arrays.copyOf(bytes, i + 1);
    }
}
