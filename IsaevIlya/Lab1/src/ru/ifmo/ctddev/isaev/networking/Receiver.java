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
            DatagramSocket socket = new DatagramSocket(4446);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
                socket.receive(packet);
                Message message = parseRelative(packet.getData());
                BroadcasterInfo info = new BroadcasterInfo(message.mac);
                broadcasters.add(info);
                pendingMessages.put(message.mac, message);
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
        message.mac = new String(mac, StandardCharsets.UTF_8);
        int hostnameLength = buffer.get();
        byte[] hostname = new byte[hostnameLength];
        buffer = buffer.get(hostname, 7, hostnameLength);
        message.hostname = new String(hostname, StandardCharsets.UTF_8);
        message.timestamp = buffer.getLong();
        return message;
    }
}
