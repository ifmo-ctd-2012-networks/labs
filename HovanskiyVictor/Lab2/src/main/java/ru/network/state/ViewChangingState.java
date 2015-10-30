package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.ServerNode;
import ru.network.Token;
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
    private boolean listenViewChanges;
    private List<DoViewChangeMessage> doViewChangeMessages = new ArrayList<>();
    private List<DiscardViewChangeMessage> discardViewChangeMessages = new ArrayList<>();

    public ViewChangingState(ServerNode node) {
        super(node);
    }

    @Override
    public void enter() {
        log.debug("enter");
        listenViewChanges = true;
        previous = System.currentTimeMillis();
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
        super.tick();
        //log.debug("tick");
        long timestamp = System.currentTimeMillis();
        if (listenViewChanges && timestamp - previous >= VIEW_CHANGE_TIMEOUT) {
            log.debug("Время ожидания " + VIEW_CHANGE_TIMEOUT + "ms истекло");
            listenViewChanges = true;

            if (discardViewChangeMessages.isEmpty()) {
                node.setToken(Token.generate());
                if (doViewChangeMessages.isEmpty()) {
                    node.setState(new ExecutingState(node));
                } else {
                    node.setState(new NormalState(node));
                }
            } else {
                node.setState(new NormalState(node));
            }
        }
    }

    @Override
    public void handleDoViewChange(DoViewChangeMessage message) {
        if (listenViewChanges) {
            doViewChangeMessages.add(message);
        }
    }

    @Override
    public void handleDiscardViewChange(DiscardViewChangeMessage message) {
        if (listenViewChanges) {
            discardViewChangeMessages.add(message);
        }
    }
}
