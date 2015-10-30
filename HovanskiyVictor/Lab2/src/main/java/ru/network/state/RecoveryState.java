package ru.network.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.ServerNode;
import ru.network.message.RecoveryMessage;
import ru.network.message.RecoveryResponseMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author victor
 */
public class RecoveryState extends State {
    private static final long RECOVERY_RESPONSE_TIMEOUT = 2000;
    private final Logger log = LoggerFactory.getLogger(RecoveryState.class);
    private long previous;
    private boolean listenRecoveryResponces;
    private List<RecoveryResponseMessage> recoveryResponses = new ArrayList<>();

    public RecoveryState(ServerNode node) {
        super(node);
    }

    @Override
    public void enter() {
        log.debug("enter");
        listenRecoveryResponces = true;
        previous = System.currentTimeMillis();
        log.debug("Доступные соседи " + node.getRing().neighbours());
        node.getRing().neighbours().forEach(neighbour -> {
            if (neighbour.isActive()) {
                node.getApplicationLayer().send(neighbour, new RecoveryMessage(node, previous));
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        long timestamp = System.currentTimeMillis();
        if (listenRecoveryResponces && timestamp - previous >= RECOVERY_RESPONSE_TIMEOUT) {
            log.debug("Время ожидания " + RECOVERY_RESPONSE_TIMEOUT + "ms истекло");
            listenRecoveryResponces = false;
            if (recoveryResponses.isEmpty()) {
                node.setState(new ViewChangingState(node));
            } else {
                for (RecoveryResponseMessage message : recoveryResponses) {
                    if (node.getOperationNumber() < message.getOperationNumber()) {
                        node.setOperationNumber(message.getOperationNumber());
                        node.setData(message.getData());
                    }
                }
                node.setState(new NormalState(node));
            }
        }
    }

    @Override
    public void leave() {
        log.debug("leave");
    }

    @Override
    public void handleRecoveryResponse(RecoveryResponseMessage message) {
        if (listenRecoveryResponces) {
            if (message.getTimestamp() == previous) {
                recoveryResponses.add(message);
            }
        }
    }
}
