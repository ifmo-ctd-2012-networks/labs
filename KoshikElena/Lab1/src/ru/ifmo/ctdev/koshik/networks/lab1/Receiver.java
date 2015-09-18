package ru.ifmo.ctdev.koshik.networks.lab1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;


public class Receiver implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(Receiver.class.getName());

    private DatagramSocket socket;
    private int port;
    private final int BUF_SIZE = 256;

    private BlockingQueue<Packet> queue;

    public Receiver(int port) throws SocketException, UnknownHostException {
        queue = new LinkedBlockingDeque<>();
        LOGGER.addHandler(new ConsoleHandler());
        LOGGER.setUseParentHandlers(false);

        this.port = port;

        socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);
    }

    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] buf = new byte[BUF_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    Packet p = new Packet(packet);
                    queue.put(p);
                    LOGGER.info("Packet was RECEIVED:" + "\n" +
                            "       MAC address: " + p.macAddress + "\n" +
                            "       Hostname: " + p.hostname + "\n" +
                            "       Unix Timestamp: " + p.timestamp + "\n");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public List<Packet> getPackets() {
        List<Packet> packets = new ArrayList<>();
        queue.drainTo(packets);
        return packets;
    }
}
