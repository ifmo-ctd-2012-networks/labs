import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author korektur
 *         15/09/2015
 */
public class Main {

    public static void main(String[] args) throws SocketException, UnknownHostException {
        Server server = new Server(8080);
        Thread serverThread = new Thread(server);
        Client client1 = new Client(8887, 8080);
        Client client2 = new Client(8082, 8080);
        Client client3 = new Client(8083, 8080);
        Thread threadClient1 = new Thread(client1);
        Thread threadClient2 = new Thread(client2);
        Thread threadClient3 = new Thread(client3);
        serverThread.start();
        threadClient1.start();
        threadClient2.start();
        threadClient3.start();
    }
}
