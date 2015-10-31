package ru.network.state;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Looper;
import ru.network.Node;
import ru.network.NodeStatus;
import ru.network.ServerNode;
import ru.network.message.*;

/**
 * @author victor
 */
public abstract class State {
    private static final long PONG_TIMEOUT = 3000;
    protected final Looper looper;
    protected final ServerNode node;
    private final Logger log = LoggerFactory.getLogger(State.class);

    public State(ServerNode node) {
        this.node = node;
        this.looper = Looper.myLooper();
    }

    public void enter() {
    }

    public void leave() {

    }

    public abstract NodeStatus getStatus();

    public void tick() {
        log.debug("TICK " + getClass().getSimpleName());
        long timestamp = System.currentTimeMillis();
        boolean decreasing = false;
        for (Node neighbour : node.getRing().neighbours()) {
            if (neighbour.isActive() && timestamp - neighbour.getTimestamp() >= PONG_TIMEOUT) {
                log.debug(neighbour + " inactive!");
                neighbour.setActive(false);
                decreasing = true;
            }
            node.getApplicationLayer().send(neighbour, new PingMessage(node));
        }
        if (decreasing) {
            decreasing();
        }
    }

    public void decreasing() {

    }

    /**
     * Получили пинг-сообщение, обязаны на него ответить
     *
     * @param message пинг-сообщение
     */
    public void handlePing(PingMessage message) {
        if (node.getStatus() != NodeStatus.INITIALIZATION) {
            node.getApplicationLayer().send(message.getSender(), new PongMessage(node));
        }
    }

    /**
     * Обновляем узел, пославший нам сообщение, как активный
     *
     * @param message ответ на пинг
     */
    public void handlePong(PongMessage message) {
        if (node.getStatus() != NodeStatus.INITIALIZATION) {
            node.getApplicationLayer().update(message.getSender());
        }
    }

    /**
     * Текущий узел получил сообщение о передаче токена
     *
     * @param message сообщение с токеном
     */
    public void handleSendToken(SendTokenMessage message) {

    }

    /**
     * Текущий узел получил запрос по предоставлению всей информации о текущем узле
     *
     * @param message запрос
     */
    public void handleGetIdentity(GetIdentityMessage message) {
        node.getApplicationLayer().broadcast(new SendIdentityMessage(node, node.getHostname(), node.getMacAddress(), node.getPort()));
    }

    public void handleSendIdentity(SendIdentityMessage message) {

    }

    public void handleReceivedToken(ReceivedTokenMessage message) {

    }

    public void handleStartViewChange(StartViewChangeMessage message) {
        if (node.getStatus() == NodeStatus.EXECUTING || node.getStatus() == NodeStatus.NORMAL) {
            assert message.getSender().isValid();
            if (node.getOperationNumber() < message.getOperationNumber() || node.getMacAddress().compareTo(message.getSender().getMacAddress()) >= 0) {
                node.getApplicationLayer().send(message.getSender(), new DoViewChangeMessage(node));
            } else {
                node.getApplicationLayer().send(message.getSender(), new DiscardViewChangeMessage(node));
            }
        }
    }

    public void handleDoViewChange(DoViewChangeMessage message) {

    }

    public void handleDiscardViewChange(DiscardViewChangeMessage message) {

    }

    public void handleRecovery(RecoveryMessage message) {
        if (node.getStatus() == NodeStatus.EXECUTING || node.getStatus() == NodeStatus.NORMAL) {
            node.getApplicationLayer().send(message.getSender(), new RecoveryResponseMessage(node, node.getOperationNumber(), node.getData(), message.getTimestamp()));
        }
    }

    public void handleRecoveryResponse(RecoveryResponseMessage message) {

    }
}
