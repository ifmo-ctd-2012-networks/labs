package ru.network.layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Looper;
import ru.network.Node;
import ru.network.ServerNode;
import ru.network.message.Message;
import ru.network.message.PingMessage;
import ru.network.message.PongMessage;

import java.net.InetSocketAddress;

/**
 * @author victor
 */
public class ApplicationLayer extends TransportLayer {
    public final static int BROADCAST_PORT = 1111;
    private final Logger log = LoggerFactory.getLogger(ApplicationLayer.class);
    private final ServerNode node;
    private final Looper looper;

    public ApplicationLayer(ServerNode node) {
        this.node = node;
        this.looper = Looper.myLooper();
    }

    public void send(Node recipient, Message message) {
        assert recipient != null : "Получатель должен быть задан";
        assert !recipient.getMacAddress().equals(node.getMacAddress()) : "Получатель должен быть отличен от отправителя";
        //assert recipient.isActive() : "Получатель должен быть активным";
        if (!PongMessage.class.isInstance(message) && !PingMessage.class.isInstance(message)) {
            log.debug("Отправка " + recipient + ": " + message);
        }
        send(recipient.getInetSocketAddress(), Message.encode(message).toString());
    }

    public void update(Node node) {
        node.setActive(true);
    }

    public void listen(int port, boolean broadcast) {
        //log.debug(node + " is listening port " + port);
        bind(port, (address, string) -> {
            Node sender = node.getRing().findNodeByAddress(address);
            assert sender != null;
            Message message = Message.decode(sender, string);
            sender.setActive(true);
            sender.setTimestamp(System.currentTimeMillis());
            if (!PongMessage.class.isInstance(message) && !PingMessage.class.isInstance(message)) {
                log.debug("Получено: " + message);
            }
            looper.add(() -> {
                message.delegate(node);
            });
        }, broadcast);
    }

    public void broadcast(Message message) {
        log.debug("Рассылка: " + message);
        broadcast(new InetSocketAddress(getInetAddress(), BROADCAST_PORT), Message.encode(message).toString());
    }
}
