import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author korektur
 *         15/09/2015
 */
public class Client implements Runnable {
    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    private static final int SO_TIMEOUT = 5000;

    private final int PORT;

    public Client(int port) {
        PORT = port;
    }

    @Override
    public void run() {
        try (DatagramSocket c = new DatagramSocket(PORT)) {
            c.setBroadcast(true);
            c.setSoTimeout(SO_TIMEOUT);
            //noinspection InfiniteLoopStatement
            while (true) {

                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                long startTime = System.currentTimeMillis();

                try {
                    c.receive(receivePacket);

                    String message = new String(receivePacket.getData()).trim();
                    LOG.info(PORT + ": " + message);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }

                long finishTime = System.currentTimeMillis();

                if (finishTime - startTime < 5000) {
                    try {
                        Thread.sleep(5000 - (finishTime - startTime));
                    } catch (InterruptedException e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        } catch (SocketException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
