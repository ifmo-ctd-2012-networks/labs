package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramSender;

import java.net.SocketException;
import java.util.Map;
import java.util.Random;

public class TrashSender extends DatagramSender implements BroadcastAnnouncer.Task {
    private static final Logger logger = Logger.getLogger(TrashSender.class);

    private final Random random = new Random();

    private TrashSender(int port) throws SocketException {
        super(port);
    }

    public static BroadcastAnnouncer.TaskCreator builder() {
        return TrashSender::new;
    }

    @Override
    public void invoke(Map<NodeInfo, Integer> missedPacketCounter) {
        int messageSize = random.nextInt(100);
        byte[] message = new byte[messageSize];
        random.nextBytes(message);

        logger.info("Broadcast trash");
        sendBytes(message);
    }
}
