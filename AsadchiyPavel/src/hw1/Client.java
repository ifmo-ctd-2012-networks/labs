package hw1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

public class Client implements Runnable {
    private static final Logger log = Logger.getLogger(Client.class.getName());
    private final Writer writer;

    public Client(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void run() {
        log.info("Client is ready to receive packets on port " + Main.port);
        try (DatagramSocket socket = new DatagramSocket(Main.port)) {
            socket.setBroadcast(true);
            byte[] packetByte = new byte[1024]; // 1kb
            DatagramPacket packet = new DatagramPacket(packetByte, packetByte.length);
            while (!Thread.interrupted()) {
                try {
                    socket.receive(packet);
                    writer.add(new ReceivedInfo(packet.getData()));
//                    log.info(Thread.currentThread().getName() + " received message: " + new ReceivedInfo(packet.getData()));
                } catch (IOException e) {
                    log.severe("Can't receive data, error message: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            log.severe("Can't create socket, error message: " + e.getMessage());
        }
    }
}
