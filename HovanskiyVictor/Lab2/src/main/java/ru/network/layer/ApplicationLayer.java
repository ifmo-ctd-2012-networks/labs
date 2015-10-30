package ru.network.layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Looper;
import ru.network.Node;
import ru.network.ServerNode;
import ru.network.message.Message;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
public class ApplicationLayer extends TransportLayer {
    private final Logger log = LoggerFactory.getLogger(ApplicationLayer.class);
    public final static int BROADCAST_PORT = 1111;
    private final ServerNode node;
    private final Looper looper;

    public ApplicationLayer(ServerNode node) {
        this.node = node;
        this.looper = Looper.myLooper();
    }

    public void send(Node recipient, Message message) {
        log.debug(node + " sends to " + recipient + ": " + message);
        send(recipient.getInetSocketAddress(), Message.encode(message).toString());
    }

    public void update(Node node) {

    }

    public void listen(int port, boolean broadcast) {
        //log.debug(node + " is listening port " + port);
        bind(port, (address, string) -> {
            log.debug("Received message: " + string + " from " + address);
            Node sender = node.getRing().findNodeByAddress(address);
            Message message = Message.decode(sender, string);
            looper.add(() -> {
                message.delegate(node);
            });
        }, broadcast);
    }

    public void broadcast(Message message) {
        //log.debug(node + " broadcasts: " + message);
        broadcast(new InetSocketAddress(getInetAddress(), BROADCAST_PORT), Message.encode(message).toString());
    }
}
