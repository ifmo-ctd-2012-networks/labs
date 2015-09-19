package ru.ifmo.ctddev.isaev.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Ilya Isaev
 */
public class Main {

    public static final Map<String, BroadcasterInfo> broadcasters = new TreeMap<>(String::compareTo);
    public static final Map<String, Message> pendingMessages = new HashMap<>();
    private static final Executor executor = Executors.newFixedThreadPool(3);
    public static int PACKET_LENGTH;
    public static int SLEEP_TIME = 5000;
    public static int PORT = 4445;
    public static String HOSTNAME;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Expected three arguments: <hostname> [port]? [timeout]?");
            return;
        }
        assert HOSTNAME.getBytes().length < 256;
        if (args.length > 1) {
            PORT = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            SLEEP_TIME = Integer.parseInt(args[2]);
        }

        HOSTNAME = args[0];
        PACKET_LENGTH = 6 + HOSTNAME.getBytes().length + 1 + 8;
        executor.execute(new Broadcaster());
        executor.execute(new Receiver());
        executor.execute(new Printer());
    }
}
