package src;

public class Main {
    private static int port = 8889;

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        Client client = new Client(port);
        Server server = new Server(port);
        new Thread(server).start();
        new Thread(client).start();
    }

}