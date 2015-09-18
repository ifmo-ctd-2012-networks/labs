import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author korektur
 *         15/09/2015
 */
public class Client implements Runnable {
    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    private static final int SO_TIMEOUT = 6000;

    private final int PORT;
    private final ConcurrentLinkedQueue<Packet> queue;

    public Client(int port, ConcurrentLinkedQueue<Packet> queue) {
        PORT = port;
        this.queue = queue;
    }

    @Override
    public void run() {
        try (DatagramSocket c = new DatagramSocket(PORT)) {
            c.setBroadcast(true);
            c.setSoTimeout(SO_TIMEOUT);
            while (!Thread.currentThread().isInterrupted()) {

                byte[] receiveBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);

                try {
                    c.receive(receivePacket);

                    Packet packet = new Packet(receivePacket);
                    queue.add(packet);
//                    LOG.info(packet.toString());
                } catch (SocketTimeoutException e) {
                    LOG.info("Socket timeout");
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }

            }
        } catch (SocketException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
