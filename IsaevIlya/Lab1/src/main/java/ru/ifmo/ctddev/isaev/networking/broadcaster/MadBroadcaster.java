package ru.ifmo.ctddev.isaev.networking.broadcaster;

import java.net.*;
import java.util.Random;

import static ru.ifmo.ctddev.isaev.networking.Main.PORT;

/**
 * @author Ilya Isaev
 */
public class MadBroadcaster extends Broadcaster {
    private static final Random RANDOM = new Random(System.nanoTime());

    @Override
    public DatagramPacket getBroadcastPacket() throws SocketException, UnknownHostException {
        int packetSize = RANDOM.nextInt(2 * 1024 * 1024);
        byte[] bytes = new byte[packetSize];
        RANDOM.nextBytes(bytes);
        NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        InetAddress broadCastAddress = getBroadcastAddress(network);
        return new DatagramPacket(bytes,
                bytes.length, broadCastAddress, PORT);
    }
}
