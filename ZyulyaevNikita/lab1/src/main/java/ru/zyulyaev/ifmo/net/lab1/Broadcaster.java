package ru.zyulyaev.ifmo.net.lab1;

import ru.zyulyaev.ifmo.net.lab1.components.Component;

import java.util.stream.Stream;

/**
 * @author zyulyaev
 */
public class Broadcaster {
    private static final int DEFAULT_PORT = 1234;

    public static void main(String[] args) {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        UdpStatistics statistics = new UdpStatistics();
        UdpServer server = new UdpServer(port, statistics);
        UdpAttacker attacker = new UdpAttacker(port);
        UdpBroadcaster broadcaster = new UdpBroadcaster(port);
        Stream.of(server, broadcaster, statistics, attacker)
                .forEach(Component::start);
    }
}
