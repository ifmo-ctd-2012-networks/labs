package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Node;
import ru.network.Ring;
import ru.network.ServerNode;
import ru.network.message.GetIdentityMessage;
import ru.network.message.SendIdentityMessage;
import ru.network.message.SendTokenMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author victor
 */
public class RecoveringState extends State {
    private final Logger log = LoggerFactory.getLogger(RecoveringState.class);
    private static final long IDENTITY_TIMEOUT = 2000;
    private long previous;
    private boolean isIdentityTimeout = false;
    private List<SendIdentityMessage> identityMessages = new ArrayList<>();

    public RecoveringState(ServerNode node) {
        super(node);
    }

    @Override
    public void tick() {
        //log.debug("tick");
        /*long timestamp = System.currentTimeMillis();
        if (!isIdentityTimeout && timestamp - previous >= IDENTITY_TIMEOUT) {
            log.debug("identity timeout");
            isIdentityTimeout = true;

            if (identityMessages.isEmpty()) {
                node.setRing(Ring.empty());
                node.setState(new ViewChangingState(node));
            } else {
                List<Node> nodes = new ArrayList<>();
                for (SendIdentityMessage message : identityMessages) {
                    Node other = new Node(message.getHostname(), message.getMacAddress(), message.getPort());
                    nodes.add(other);
                }
                node.setRing(new Ring(nodes));
                node.setState(new NormalState(node));
            }
        }*/
    }

    @Override
    public void handleSendIdentity(SendIdentityMessage message) {
        if (!isIdentityTimeout) {
            identityMessages.add(message);
        }
    }

    @Override
    public void leave() {
        log.debug("leave");
    }

    @Override
    public void enter() {
        log.debug("enter");
        previous = System.currentTimeMillis();
        node.getStateLog().restore();
        if (node.getStateLog().isEmpty()) {
            node.getApplicationLayer().broadcast(new GetIdentityMessage(node));
        } else {
            //node.getApplicationLayer();
        }
    }

    @Override
    public void handleSendToken(SendTokenMessage message) {
        super.handleSendToken(message);
    }

    protected boolean isEmptyLog() {
        return true;
    }
}
