package ru.zyulyaev.ifmo.net.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zyulyaev.ifmo.net.lab1.components.BackgroundComponent;
import ru.zyulyaev.ifmo.net.lab1.components.ComponentInitializationException;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zyulyaev
 */
public class UdpStatistics extends BackgroundComponent {
    private static final Logger log = LoggerFactory.getLogger(UdpStatistics.class);
    private static final int DROP_TRESHOLD = 5;
    private final Object lock = new Object();

    private final Map<SocketAddress, UdpMessage> lastData = new HashMap<>();
    private final Map<SocketAddress, Integer> missed = new HashMap<>();
    private final Set<SocketAddress> sinceLast = new HashSet<>();


    public void processMessage(DatagramPacket packet) {
        synchronized (lock) {
            SocketAddress address = packet.getSocketAddress();
            try {
                lastData.put(address, UdpMessage.fromPacket(packet));
                sinceLast.add(address);
            } catch (MessageMalformed messageMalformed) {
                log.warn("Received malformed message from " + address + ". Skip.");
            }
        }
    }

    @Override
    protected void init() throws ComponentInitializationException {
    }

    @Override
    protected void doInBackground() {
        try {
            while (!Thread.interrupted()) {
                synchronized (lock) {
                    for (SocketAddress addr : lastData.keySet()) {
                        if (sinceLast.contains(addr)) {
                            missed.remove(addr);
                        } else {
                            missed.merge(addr, 1, Integer::sum);
                        }
                    }
                    sinceLast.clear();
                    Set<SocketAddress> drop = missed.entrySet().stream()
                            .filter(e -> e.getValue() >= DROP_TRESHOLD)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet());
                    drop.forEach(lastData::remove);
                    drop.forEach(missed::remove);

                    System.out.println("===========================");
                    for (Map.Entry<SocketAddress, UdpMessage> entry : lastData.entrySet()) {
                        SocketAddress addr = entry.getKey();
                        UdpMessage message = entry.getValue();
                        System.out.println(addr + ": " + message);
                    }
                    System.out.println("===========================");
                    System.out.println();
                }
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
        }
    }

    @Override
    protected void cleanup() {

    }
}
