package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Node;
import ru.network.ServerNode;
import ru.network.message.ReceivedTokenMessage;
import ru.network.message.SendTokenMessage;
import ru.network.message.StartViewChangeMessage;

/**
 * @author victor
 */
public class NormalState extends State {
    private final Logger log = LoggerFactory.getLogger(NormalState.class);

    public NormalState(ServerNode node) {
        super(node);
    }

    @Override
    public void handleSendToken(SendTokenMessage message) {
        if (node.getOperationNumber() < message.getOperationNumber()) {
            node.setOperationNumber(message.getOperationNumber());
            node.setToken(message.getToken());
            node.setData(message.getData());
        }
        node.getApplicationLayer().send(message.getSender(), new ReceivedTokenMessage(node, message.getToken()));
    }

    @Override
    public void enter() {
        log.debug("enter");
    }

    @Override
    public void tick() {
        super.tick();
        if (node.hasToken()) {
            node.setState(new ExecutingState(node));
        }
    }

    @Override
    public void decreasing() {
        log.debug("Количество активных соседей уменьшилось");
        node.setState(new ViewChangingState(node));
    }

    @Override
    public void leave() {
        log.debug("leave");
    }
}
