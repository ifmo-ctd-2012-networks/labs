package ru.ifmo.network;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class DatagramReceiver implements Runnable {
    public final int listeningPort;

    protected final Logger logger = Logger.getLogger(DatagramReceiver.class);

    private byte[] packetBuf = new byte[1500];

    protected DatagramReceiver(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(listeningPort);

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(packetBuf, packetBuf.length);
                socket.receive(packet);

                onReceive(packet.getData(), packet.getLength());
            }
        } catch (IOException e) {
            logger.error("Listening socket failed", e);
        }
    }

    protected abstract void onReceive(byte[] bytes, int length) throws IOException;
}
