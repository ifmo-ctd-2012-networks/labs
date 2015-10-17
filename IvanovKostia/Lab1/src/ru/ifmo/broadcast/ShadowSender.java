package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.Message;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramSender;

import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShadowSender extends DatagramSender implements BroadcastAnnouncer.Task, Cloneable {
    private static final Logger logger = Logger.getLogger(ShadowSender.class);

    private final Map<NodeInfo, Long> shadowedNodes = new ConcurrentHashMap<>();
    private final Map<NodeInfo, Integer> skipCreatingShadow = new ConcurrentHashMap<>();

    private final int livingTime;

    private ShadowSender(int port, int livingTime) throws SocketException {
        super(port);

        this.livingTime = livingTime;
    }

    public static BroadcastAnnouncer.TaskCreator builder(int livingTime) {
        return port -> new ShadowSender(port, livingTime);
    }

    @Override
    public void invoke(Map<NodeInfo, Integer> missedPacketCounter) {
        missedPacketCounter.forEach((info, value) -> {
            shadowedNodes.computeIfAbsent(info, nodeInfo -> {
                if (value > BroadcastAnnouncer.LOST_THRESHOLD * 0.7 && !skipCreatingShadow.containsKey(info)) {
                    logger.info("Creating a shadow of " + info);
                    return System.currentTimeMillis();
                }
                return null;
            });
        });

        shadowedNodes.forEach((info, startTime) -> {
            long curTime = System.currentTimeMillis() - startTime;
            if (curTime < livingTime) {
                send(info);
            } else {
                skipCreatingShadow.put(info, BroadcastAnnouncer.LOST_THRESHOLD * 2);
                shadowedNodes.remove(info);
                logger.info(String.format("Shadow of %s expires", info));
            }
        });
    }

    private void send(NodeInfo info) {
        Message message = info.toMessage();
        logger.info(String.format("Broadcast shadow %s", message));
        sendBytes(message.toBytes());
    }

}

