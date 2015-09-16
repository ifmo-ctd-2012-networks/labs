package ru.ifmo.ctddev.isaev.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static ru.ifmo.ctddev.isaev.networking.Broadcaster.PACKET_LENGTH;
import static ru.ifmo.ctddev.isaev.networking.Broadcaster.getBroadcastAddress;

/**
 * @author Ilya Isaev
 */
public class Receiver implements Runnable {
    private final Set<BroadcasterInfo> broadcasters = new TreeSet<>((o1, o2) -> {
        return o1.mac.compareTo(o2.mac);
    });

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.connect(getBroadcastAddress(), Broadcaster.PORT);
            socket.setBroadcast(true);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
                socket.receive(packet);
                BroadcasterInfo broadcasterInfo = parseRelative(packet.getData());
                broadcasters.add(broadcasterInfo);
                printInfo();
                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printInfo() {
        System.out.println("Broadcasters: ");
        for (BroadcasterInfo broadcasterInfo : broadcasters) {
            System.out.println(String.format("mac: %s , hostname = %s", Arrays.toString(broadcasterInfo.mac.getBytes()), broadcasterInfo.hostname));
        }
        System.out.println("_____________________");
    }

    private BroadcasterInfo parseRelative(byte[] data) {
        BroadcasterInfo broadcasterInfo = new BroadcasterInfo();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] mac = new byte[6];
        broadcasterInfo.mac = new String(buffer.get(mac, 0, 6).array(), StandardCharsets.UTF_8);
        int hostnameLength = buffer.get(6);
        broadcasterInfo.hostname = new String(buffer.get(mac, 7, hostnameLength).array(), StandardCharsets.UTF_8);
        return broadcasterInfo;
    }
}
