import java.net.SocketException;

/**
 * Created by Георгий on 26.09.2015.
 */
public class Main {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Server server = null;
        try {
            server = new Server(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Client client = new Client(port);
        Printer printer = new Printer(server);
        server.start();
        client.start();
        printer.start();

    }
}
