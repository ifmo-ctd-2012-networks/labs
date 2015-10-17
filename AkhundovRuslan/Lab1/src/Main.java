import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author korektur
 *         15/09/2015
 */
public class Main {

    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
        Server server = new Server(1234);
        Thread serverThread = new Thread(server);

        ConcurrentLinkedQueue<Packet> packetsQueue = new ConcurrentLinkedQueue<>();

        Client client = new Client(1234, packetsQueue);
        Thread threadClient = new Thread(client);

        Printer printer = new Printer(packetsQueue);
        Thread printerThread = new Thread(printer);

        serverThread.start();
        threadClient.start();
        printerThread.start();

        printerThread.join();
        threadClient.join();
        serverThread.join();
    }
}
