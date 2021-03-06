package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.NodeStatus;
import ru.network.ServerNode;
import ru.network.message.GetIdentityMessage;
import ru.network.message.SendIdentityMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author victor
 */
public class InitializationState extends State {
    private static final long IDENTITY_TIMEOUT = 5000;
    private final Logger log = LoggerFactory.getLogger(InitializationState.class);
    private long previous;
    private boolean listenIdentities;

    private List<SendIdentityMessage> identityMessages = new ArrayList<>();

    public InitializationState(ServerNode node) {
        super(node);
    }

    @Override
    public void tick() {
        super.tick();
        //log.debug("tick");
        long timestamp = System.currentTimeMillis();
        if (listenIdentities && timestamp - previous >= IDENTITY_TIMEOUT) {
            log.debug("Время ожидания " + IDENTITY_TIMEOUT + "ms истекло");
            listenIdentities = false;

            assert !identityMessages.isEmpty() : "Никто не отозвался на broadcast";

            for (SendIdentityMessage message : identityMessages) {
                message.getSender().setHostname(message.getHostname());
                message.getSender().setPort(message.getPort());
                message.getSender().setMacAddress(message.getMacAddress());
                log.debug("Добавляем в кольцо " + message.getSender());
                node.getRing().put(message.getSender());
            }

            log.info("Кольцо: " + node.getRing());

            node.getStateLog().save();

            node.setState(new RecoveryState(node));
        }
    }

    @Override
    public void handleSendIdentity(SendIdentityMessage message) {
        if (listenIdentities) {
            identityMessages.add(message);
        }
    }

    @Override
    public void leave() {
        log.debug("leave");
    }

    @Override
    public NodeStatus getStatus() {
        return NodeStatus.INITIALIZATION;
    }

    @Override
    public void enter() {
        log.debug("enter");
        previous = System.currentTimeMillis();
        node.getStateLog().restore();
        if (node.getRing().all().isEmpty()) {
            listenIdentities = true;
            node.getApplicationLayer().broadcast(new GetIdentityMessage(node));
        } else {
            node.setState(new RecoveryState(node));
        }
    }

    protected boolean isEmptyLog() {
        return true;
    }
}
