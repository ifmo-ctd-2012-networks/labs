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
                DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
                socket.receive(packet);
                Message message = parseRelative(packet.getData());
                System.out.println("Received message: " + message);
                BroadcasterInfo info = new BroadcasterInfo(message);
                synchronized (broadcasters) {
                    synchronized (pendingMessages) {
                        if (broadcasters.putIfAbsent(info.mac, info) == null) {
                            System.out.format("Founded new neighbour: mac: %d, hostname: \"%s\"\n",
                                    info.mac, info.hostname);
                        }
                        pendingMessages.put(message.mac, message);
                    }
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
        message.mac = 0L;
        message.mac +=
                ((long) mac[0] << 36) +
                        ((long) mac[1] << 30) +
                        ((long) mac[2] << 24) +
                        ((long) mac[3] << 16) +
                        ((long) mac[4] << 8) +
                        ((long) mac[5]);
        int hostnameLength = buffer.get();
        byte[] hostname = new byte[hostnameLength];
        buffer = buffer.get(hostname, 0, hostnameLength);
        message.hostname = new String(hostname, StandardCharsets.UTF_8);
        message.timestamp = buffer.getLong();
        return message;
    }
}
