package ru.ifmo.isomurodov.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Created by javlon on 18.09.15.
 */
public class Receiving implements Runnable {
    private final int port;
    private final DB db;

    public Receiving(int port, DB db) {
        this.port = port;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            if (!socket.getBroadcast()) {
                throw new IllegalArgumentException("Can't bind to broadcast");
            }
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[266], 266);
                try {
                    socket.receive(packet);
                    byte[] bytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                    if(bytes.length >= 11) {
                        if(bytes.length == (int) bytes[6] + 11) {
                            UDP message = new UDP(bytes);
                            db.add(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
}
