import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Main <port number> ");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            Thread server = new Thread(new Server(port));
            Thread client = new Thread(new Client(port));
            server.start();
            client.start();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
        }
    }
}
