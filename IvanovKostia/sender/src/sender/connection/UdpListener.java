package sender.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.function.Consumer;

public class UdpListener implements Runnable {
    public static final int RESTORE_ATTEMPTS_DELAY = 1000;

    private final int port;
    private DatagramSocket socket;

    private final Consumer<byte[]> dataConsuner;

    public UdpListener(int port, Consumer<byte[]> dataConsuner) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
        this.dataConsuner = dataConsuner;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] bytes = new byte[1500];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);

                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
                    dataConsuner.accept(data);
                } catch (IOException e) {
                    restore();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void restore() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {

            try {
                try {
                    socket.close();
                } catch (Throwable ignored) {
                }
                socket = new DatagramSocket(port);
            } catch (SocketException e) {
                Thread.sleep(RESTORE_ATTEMPTS_DELAY);
            }
        }
        throw new InterruptedException();
    }
}
