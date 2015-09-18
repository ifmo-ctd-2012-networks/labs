package ru.ifmo.network;

import org.apache.log4j.Logger;
import ru.ifmo.threads.ClosableRunnable;

import java.io.IOException;
import java.net.*;

public abstract class DatagramReceiver implements ClosableRunnable {
    protected final Logger logger = Logger.getLogger(DatagramReceiver.class);

    public final int listenedPort;
    private final DatagramSocket datagramSocket;

    private boolean closed;

    private byte[] packetBuf = new byte[1500];

    protected DatagramReceiver(int listenedPort) throws SocketException {
        this.listenedPort = listenedPort;
        try {
            datagramSocket = new DatagramSocket(listenedPort, InetAddress.getByName("0.0.0.0"));
        } catch (UnknownHostException e) {
            throw new Error("Unexpected exception", e);
        }
    }

    public void run() {
        try {
            DatagramSocket socket = datagramSocket;

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(packetBuf, packetBuf.length);
                socket.receive(packet);

                onReceive(packet.getData(), packet.getLength());
            }
        } catch (IOException e) {
            if (!closed) {
                logger.error("Socket listening failed", e);
            }
        } catch (Exception e) {
            logger.error("Receiver shutdown due to exception", e);
        } finally {
            close();
        }
    }

    public void close() {
        closed = true;
        datagramSocket.close();
    }

    protected abstract void onReceive(byte[] bytes, int length) throws IOException;
}
