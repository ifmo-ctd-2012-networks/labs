package ru.ifmo.broadcast;

import ru.ifmo.info.NodeInfo;

import java.io.IOException;
import java.net.SocketException;
import java.util.Random;

public class BroadcastAnnouncerTest {
    private final Random random = new Random();
    private int hostCounter = 0;

    public void testMain() throws Exception {
        for (int i = 0; i < 2; i++) {
            addBroadcastAnnouncer(5_000L);
        }

        System.in.read();
    }

    private void addBroadcastAnnouncer(final long sendDelay) throws SocketException {
        NodeInfo nodeInfo = generateNodeInfo();
        new BroadcastAnnouncer(new Random().nextInt(12345), sendDelay) {
            @Override
            protected NodeInfo myNodeInfo() throws IOException {
                return nodeInfo;
            }
        }.start();
    }

    private NodeInfo generateNodeInfo() {
        int[] ints = random.ints()
                .limit(MacAddress.SIZE)
                .toArray();
        byte[] bytes = new byte[MacAddress.SIZE];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) ints[i];
        }

        return new NodeInfo(new MacAddress(bytes), String.format("Host #%d", hostCounter++));
    }


}