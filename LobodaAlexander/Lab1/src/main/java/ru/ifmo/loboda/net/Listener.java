package ru.ifmo.loboda.net;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Listener implements Runnable {
    private final int port;
    private final DB db;
    public static final int MAX_PACKET_SIZE = 266;

    public Listener(int port, DB db) {
        this.port = port;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            DatagramSocket s = new DatagramSocket(port);
            if(!s.getBroadcast()){
                System.err.println("Can't bind broadcast channel");
                System.exit(1);
            }
            while(true){
                DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
                try {
                    s.receive(packet);
                    byte[] bytes = packet.getData();
                    try {
                        Packet info = new Packet(Arrays.copyOfRange(bytes, 0, packet.getLength()));
                        db.update(info);
                    } catch (BadPacketException e) {
                        System.err.println(packet.getAddress().getHostAddress() + " sent wrong UDP packet: " + e.getMessage());
                    }
                } catch (IOException ignored) {}
            }
        } catch (BindException e) {
            System.err.println("Announcer is already working");
            System.exit(1);
        } catch (SocketException e) {
            System.err.println("Crashed");
            System.exit(1);
        }
    }
}
