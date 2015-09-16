package ru.ifmo.ctddev.isaev.networking;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Ilya Isaev
 */
public class Main {

    public static final int PACKET_LENGTH = 128;
    private static final Executor executor = Executors.newFixedThreadPool(3);
    public static int PORT = 4445;
    public static String HOSTNAME;
    public static Set<BroadcasterInfo> broadcasters = new ConcurrentSkipListSet<>((o1, o2) -> {
        return o1.mac.compareTo(o2.mac);
    });
    public static Map<String, Message> pendingMessages = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected two arguments: <hostname> <port>");
        }
        PORT = Integer.parseInt(args[1]);
        HOSTNAME = args[0];
        executor.execute(new Broadcaster());
        executor.execute(new Receiver());
        executor.execute(new Printer());
    }
}
