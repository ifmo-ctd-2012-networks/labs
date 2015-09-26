package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.Message;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramSender;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Map;
import java.util.Optional;

public class NiceSender extends DatagramSender implements BroadcastAnnouncer.Task, Cloneable {
    private static final Logger logger = Logger.getLogger(NiceSender.class);

    private final NodeInfo myNodeInfo;

    private NiceSender(int port, NetworkInterface network) throws SocketException {
        super(port);

        this.myNodeInfo = Optional.ofNullable(NodeInfo.atNetworkInterface(network))
                .orElseThrow(() -> new IllegalArgumentException("Cannot send at name of network interface " + network));
    }

    public static BroadcastAnnouncer.TaskCreator builder(NetworkInterface network) {
        return port -> new NiceSender(port, network);
    }

    @Override
    public void invoke(Map<NodeInfo, Integer> missedPacketCounter) {
        NodeInfo info = myNodeInfo;
        Message message = info.toMessage();
        logger.info(String.format("Broadcast %s", message));
        sendBytes(message.toBytes());
    }

}

