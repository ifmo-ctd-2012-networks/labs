package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.LocalInfoGenerator;
import ru.ifmo.info.NodeInfo;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;

public class BroadcastAnnouncerTest {
    private final Logger logger = Logger.getLogger(BroadcastAnnouncerTest.class);

    private static final int MINIMAL_PORT = 1025;
    private static final int MAXIMAL_PORT = 65536;
    private final PrimitiveIterator.OfInt portIterator = new Random().ints(MINIMAL_PORT, MAXIMAL_PORT)
            .distinct()
            .limit(MAXIMAL_PORT - MINIMAL_PORT)
            .iterator();

    private final int announcersLimit = 4;
    private final Iterator<NodeInfo> infoIterator = new LocalInfoGenerator();


    private Iterable<NodeInfo> infoGenerator;

    private List<BroadcastAnnouncer> announcers = new ArrayList<>();

    public BroadcastAnnouncerTest() throws SocketException {
        infoGenerator = () -> infoIterator;
    }

    public static void main(String[] args) throws Exception {
        new BroadcastAnnouncerTest().test();
    }

    public void test() throws Exception {
        int limit = announcersLimit;
        for (NodeInfo nodeInfo : infoGenerator) {
            addBroadcastAnnouncer(nodeInfo, 5_000L);
            logger.info("Introduced new announcer at " + nodeInfo);

            if (--limit <= 0) break;
        }

        System.in.read();

        announcers.forEach(BroadcastAnnouncer::close);
    }

    private void addBroadcastAnnouncer(NodeInfo nodeInfo, long sendDelay) throws IOException {
        if (portIterator.hasNext()) {
            BroadcastAnnouncer announcer = new BroadcastAnnouncer(portIterator.next(), sendDelay, nodeInfo)
                    .start();
            announcers.add(announcer);
        }
    }

}