
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            Server server = new Server(port);
            Client client = new Client(port);
            Controller controller = new Controller(server);
            server.start();
            client.start();
            controller.start();
            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                sc.nextLine();
            }
            controller.close();
            client.close();
            server.close();

        } catch (SocketException e) {
            System.err.println("Error while initializing server: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Error while initializing client: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error while parsing port number: " + e.getMessage());
        }
    }

}
