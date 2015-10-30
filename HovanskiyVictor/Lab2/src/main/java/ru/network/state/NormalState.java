package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Node;
import ru.network.ServerNode;
import ru.network.message.ReceivedTokenMessage;
import ru.network.message.SendTokenMessage;

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
        node.getApplicationLayer().send(message.getSender(), new ReceivedTokenMessage(node));
        if (node.hasToken()) {
            node.setState(new ExecutingState(node));
        }
    }

    @Override
    public void enter() {
        log.debug("enter");
        if (node.hasToken()) {
            node.setState(new ExecutingState(node));
        }
    }

    @Override
    public void leave() {
        log.debug("leave");
    }
}
