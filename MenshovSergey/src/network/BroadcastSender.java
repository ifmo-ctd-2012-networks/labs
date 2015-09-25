package network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;

import java.time.Instant;

/**
 * Created by sergej on 14.09.15.
 */
public class BroadcastSender implements Runnable{
    private final String ipBroadcast="255.255.255.255";
    private  byte[] message;
    private DatagramSocket socket;
    private final int port = 8888;
    private final long time = 5000;
    private  String macAddress = "";
    public BroadcastSender(byte[] macAddr, String hostName) {
        macAddress = getMacaddr(macAddr);
        byte[] temp = new byte[0];
        try {
            temp = hostName.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        message = new byte[6 + 1 + temp.length + Long.BYTES];
        System.arraycopy(macAddr, 0, message, 0, 6);
        message[6] = (byte)hostName.length();

        System.arraycopy(temp,0,message,7, temp.length);
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();

        }
    }
    @Override
    public void run() {
        DatagramPacket packet;
        while (true) {
            setUnixTimestamp(message);
            try {
                packet = new DatagramPacket(message, message.length, InetAddress.getByName(ipBroadcast), port);
                socket.send(packet);
                Thread.sleep(time);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
    private String getMacaddr(byte[] message) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X:",message[i]));
        }
        return sb.toString();
    }
    private long getUnixTimestamp() {
        return Instant.now().getEpochSecond();
    }

    private void setUnixTimestamp(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(getUnixTimestamp());
        System.arraycopy(buffer.array(),0,message, message.length - Long.BYTES, Long.BYTES);
    }
}
