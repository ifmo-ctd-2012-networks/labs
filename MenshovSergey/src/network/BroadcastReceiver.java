package network;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sergej on 14.09.15.
 */
public class BroadcastReceiver  implements Runnable {
    private final String listenBroadcast = "0.0.0.0";
    private final ConcurrentHashMap<String, Long> instances;
    int port = 8888;
    DatagramSocket socket;
    int length = 1024;
    public BroadcastReceiver() {
        instances = new ConcurrentHashMap<>();
        try {
            socket = new DatagramSocket(port, InetAddress.getByName(listenBroadcast));
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        byte[] buf = new byte[length];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while(true) {
            try {
                socket.receive(packet);
                System.out.println(packet.getAddress());
                System.out.println("receiver " + getMacaddr(packet.getData()));
                System.out.println("......");
                String macAddr = getMacaddr(packet.getData());
                instances.put(macAddr, System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private Message getMessage(byte[] message) {
        byte[] mac = new byte[6];
        System.arraycopy(message, 0, mac, 0, 6);
        int len = (((int) message[6]) + 256) % 256;
        byte[] hostNameBytes = new byte[len];
        System.arraycopy(message, 7, hostNameBytes, 0, len);
        String hostName = null;
        try {
            hostName = new String(hostNameBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] timeStampBytes = new byte[Long.BYTES];
        System.arraycopy(message, message[6] + 7, timeStampBytes, 0, Long.BYTES);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(timeStampBytes);
        buffer.flip();
        long timeStamp = buffer.getLong();
        return new Message(mac, hostName, timeStamp);
    }

    private String getMacaddr(byte[] message) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X:",message[i]));
        }
        return sb.toString();
    }


    public ConcurrentHashMap<String, Long> getInstances() {
        return instances;
    }
}
