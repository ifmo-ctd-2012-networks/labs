package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Node;
import ru.network.ServerNode;
import ru.network.message.DiscardViewChangeMessage;
import ru.network.message.DoViewChangeMessage;
import ru.network.message.StartViewChangeMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author victor
 */
public class ViewChangingState extends State {
    private final Logger log = LoggerFactory.getLogger(ViewChangingState.class);
    private static final long VIEW_CHANGE_TIMEOUT = 2000;
    private long previous;
    private boolean isViewChangeTimeout = false;
    private List<DoViewChangeMessage> doViewChangeMessages = new ArrayList<>();
    private List<DiscardViewChangeMessage> discardViewChangeMessages = new ArrayList<>();

    public ViewChangingState(ServerNode node) {
        super(node);
    }

    @Override
    public void enter() {
        log.debug("enter");
        assert node.getRing() != null;
        node.getRing().neighbours().forEach(neighbour -> {
            node.getApplicationLayer().send(neighbour, new StartViewChangeMessage(node, node.getOperationNumber()));
        });
    }

    @Override
    public void leave() {
        log.debug("leave");
    }

    @Override
    public void tick() {
        log.debug("tick");
        long timestamp = System.currentTimeMillis();
        if (!isViewChangeTimeout && timestamp - previous >= VIEW_CHANGE_TIMEOUT) {
            log.debug("view change timeout");
            isViewChangeTimeout = true;

            if (discardViewChangeMessages.isEmpty()) {
                if (doViewChangeMessages.isEmpty()) {
                    // generating token
                    String newToken = "";
                    node.setToken(newToken);
                    node.setState(new ExecutingState(node));
                } else {
                    node.setState(new NormalState(node));
                }
            } else {
                node.setState(new NormalState(node));
            }
        }
    }
}
