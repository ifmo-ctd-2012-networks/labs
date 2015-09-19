package ru.ifmo.ctddev.isaev.networking;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            assert mac.length == 6;
            byte[] host = HOSTNAME.getBytes(StandardCharsets.UTF_8);
            ByteBuffer header = ByteBuffer.allocate(7 + host.length);
            InetAddress broadCastAddress = getBroadcastAddress(network);
            header.put(mac);
            header.put((byte) host.length);
            header.put(host);
            while (true) {
                try {
                    ByteBuffer toSend = ByteBuffer.allocate(PACKET_LENGTH);
                    toSend.put(header.array());
                    long timestamp = System.currentTimeMillis() / 1000;
                    toSend.putInt((int) (timestamp << 32 >> 32));
                    toSend.putInt((int) (timestamp >> 32));
                    DatagramPacket packet = new DatagramPacket(toSend.array(),
                            toSend.array().length, broadCastAddress, PORT);
                    socket.send(packet);
                    Thread.sleep(SLEEP_TIME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
    }


}
