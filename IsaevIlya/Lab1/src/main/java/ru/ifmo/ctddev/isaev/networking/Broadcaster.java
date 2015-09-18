package ru.ifmo.ctddev.isaev.networking;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static ru.ifmo.ctddev.isaev.networking.Main.*;

/**
 * @author Ilya Isaev
 */
public class Broadcaster implements Runnable {


    public static InetAddress getBroadcastAddress(NetworkInterface network) throws SocketException {
        for (InterfaceAddress interfaceAddress :
                network.getInterfaceAddresses()) {
            InetAddress broadcast = interfaceAddress.getBroadcast();
            if (broadcast != null) {
                return broadcast;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(SENDER_PORT);
            socket.setBroadcast(true);
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = Arrays.copyOf(UUID.randomUUID().toString().getBytes(), 6);
            byte[] host = HOSTNAME.getBytes(StandardCharsets.UTF_8);
            ByteBuffer header = ByteBuffer.allocate(7 + host.length);
            InetAddress broadCastAddress = getBroadcastAddress(network);
            header.put(mac);
            header.put((byte) host.length);
            header.put(host);
            while (true) {
                ByteBuffer toSend = ByteBuffer.allocate(PACKET_LENGTH);
                toSend.put(header.array());
                toSend.putLong(System.currentTimeMillis());
                DatagramPacket packet = new DatagramPacket(toSend.array(),
                        toSend.array().length, broadCastAddress, RECEIVER_PORT);
                socket.send(packet);
                Thread.sleep(SLEEP_TIME);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
