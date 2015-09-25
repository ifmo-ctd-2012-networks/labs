package ru.ifmo.ctddev.isaev.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
                    System.out.println("Received new message: ");
                    Message message = parseRelative(packet.getData());
                    if (!message.ok) {
                        System.out.println("Received bad packet");
                        continue;
                    }
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
                    System.out.println("Received bad packet");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Message parseRelative(byte[] data) {
        Message message = new Message();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        byte[] mac = new byte[6];
        buffer.position(0);
        if (buffer.remaining() < 6) {
            message.ok = false;
            return message;
        }
        buffer = buffer.get(mac);
        message.mac = Integer.toHexString(mac[0] & 0xFF) + "::" +
                Integer.toHexString(mac[1] & 0xFF) + "::" +
                Integer.toHexString(mac[2] & 0xFF) + "::" +
                Integer.toHexString(mac[3] & 0xFF) + "::" +
                Integer.toHexString(mac[4] & 0xFF) + "::" +
                Integer.toHexString(mac[5] & 0xFF);
        if (buffer.remaining() < 1) {
            message.ok = false;
            return message;
        }
        int hostnameLength = buffer.get();
        if (hostnameLength < 0) {
            message.ok = false;
            return message;
        }
        byte[] hostname = new byte[hostnameLength];
        if (buffer.remaining() < hostnameLength) {
            message.ok = false;
            return message;
        }
        buffer = buffer.get(hostname);
        message.hostname = new String(hostname, StandardCharsets.UTF_8);
        System.out.println("Hostname is: " + message.hostname);
        if (buffer.remaining() < 8) {
            message.ok = false;
            return message;
        }
        message.timestamp = buffer.getLong();
        message.ok = true;
        return message;
    }
}
