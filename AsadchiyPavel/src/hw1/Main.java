package hw1;

import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {

    public static int port;
    public static final int millisecondsSleepServer = 5_000;
    public static final int millisecondsSleepWriter = 5_000;
    public static final int maxMissPackets = 5;

    public static void main(String[] args) throws UnknownHostException, SocketException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Enter correct port in arguments");
        }
        port = Integer.parseInt(args[0]);
        Writer writer = new Writer();
        Thread writerThread = new Thread(writer, "Writer");
        Thread server = new Thread(new Server(), "Thread");
        Thread client = new Thread(new Client(writer), "Client");
        client.start();
        server.start();
        writerThread.start();
    }
}
