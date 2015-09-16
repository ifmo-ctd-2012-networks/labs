import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author korektur
 *         15/09/2015
 */
public class Main {

    public static void main(String[] args) throws SocketException, UnknownHostException {
        Server server = new Server(8080);
//        Server server2 = new Server(8889, 8080);
//        Server server3 = new Server(8887, 8080);
        Thread serverThread = new Thread(server);
//        Thread serverThread2 = new Thread(server2);
//        Thread serverThread3 = new Thread(server3);
        Client client1 = new Client(8080);
        Thread threadClient1 = new Thread(client1);
        serverThread.start();
//        serverThread2.start();
//        serverThread3.start();
        threadClient1.start();
    }
}
