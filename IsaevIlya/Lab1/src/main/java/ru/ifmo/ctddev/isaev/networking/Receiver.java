package ru.ifmo.ctddev.isaev.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static ru.ifmo.ctddev.isaev.networking.Main.*;

/**
 * @author Ilya Isaev
 */
public class Receiver implements Runnable {

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
                    socket.receive(packet);

                    Message message = parseRelative(packet.getData());
                    System.out.println("Received new message: ");
                    message.printAsTable();
                    BroadcasterInfo info = new BroadcasterInfo(message);
                    synchronized (broadcasters) {
                        synchronized (pendingMessages) {
                            if (broadcasters.putIfAbsent(info.mac, info) == null) {
                                System.out.format("Founded new neighbour: mac: %s, hostname: \"%s\"\n",
                                        info.mac, info.hostname);
                            }
                            pendingMessages.put(message.mac, message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Message parseRelative(byte[] data) {
        Message message = new Message();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] mac = new byte[6];
        buffer = buffer.get(mac, 0, 6);
        message.mac = Integer.toHexString(mac[0] & 0xFF) + "::" +
                Integer.toHexString(mac[1] & 0xFF) + "::" +
                Integer.toHexString(mac[2] & 0xFF) + "::" +
                Integer.toHexString(mac[3] & 0xFF) + "::" +
                Integer.toHexString(mac[4] & 0xFF) + "::" +
                Integer.toHexString(mac[5] & 0xFF);
        int hostnameLength = buffer.get();
        byte[] hostname = new byte[hostnameLength];
        buffer = buffer.get(hostname, 0, hostnameLength);
        message.hostname = new String(hostname, StandardCharsets.UTF_8);
        long timestamp1 = buffer.getInt();
        long timestamp2 = buffer.getInt();
        message.timestamp = timestamp1 + (timestamp2 << 32);
        return message;
    }
}
