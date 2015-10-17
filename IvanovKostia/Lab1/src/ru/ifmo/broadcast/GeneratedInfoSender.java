package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.MacAddress;
import ru.ifmo.info.Message;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramSender;

import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratedInfoSender extends DatagramSender implements BroadcastAnnouncer.Task, Cloneable {
    private static final Logger logger = Logger.getLogger(GeneratedInfoSender.class);

    private static final AtomicInteger hostnameCounter = new AtomicInteger();

    private final List<NodeInfo> myNodeInfo;

    private GeneratedInfoSender(int port, int senderNum) throws SocketException {
        super(port);
        myNodeInfo = Stream.generate(() -> new NodeInfo(MacAddress.random(), String.format("Martoon-random-#%d", hostnameCounter.getAndIncrement())))
                .limit(senderNum)
                .collect(Collectors.toList());
    }

    public static BroadcastAnnouncer.TaskCreator builder(int senderNum) {
        return port -> new GeneratedInfoSender(port, senderNum);
    }

    @Override
    public void invoke(Map<NodeInfo, Integer> missedPacketCounter) {
        for (NodeInfo info : myNodeInfo) {
            Message message = info.toMessage();
            logger.info(String.format("Broadcast %s", message));
            sendBytes(message.toBytes());
        }
    }

}

