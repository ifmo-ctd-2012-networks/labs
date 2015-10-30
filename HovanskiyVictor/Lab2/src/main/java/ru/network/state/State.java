package ru.network.state;


import ru.network.Looper;
import ru.network.NodeStatus;
import ru.network.ServerNode;
import ru.network.message.*;

/**
 * @author victor
 */
public abstract class State {

    protected final Looper looper;
    protected final ServerNode node;

    public State(ServerNode node) {
        this.node = node;
        this.looper = Looper.myLooper();
    }

    public void enter() {

    }

    public void leave() {

    }

    public void tick() {
        //node.getApplicationLayer().se;
    }

    /**
     * Получили пинг-сообщение, обязаны на него ответить
     * @param message пинг-сообщение
     */
    public void handlePing(PingMessage message) {
        node.getApplicationLayer().send(message.getSender(), new PongMessage(node));
    }

    /**
     * Обновляем узел, пославший нам сообщение, как активный
     * @param message ответ на пинг
     */
    public void handlePong(PongMessage message) {
        node.getApplicationLayer().update(message.getSender());
    }

    /**
     * Текущий узел получил сообщение о передаче токена
     * @param message сообщение с токеном
     */
    public void handleSendToken(SendTokenMessage message) {
        if (node.getOperationNumber() >= message.getOperationNumber()) {
            return;
        }
        node.setOperationNumber(message.getOperationNumber());
        node.setData(message.getData());
        node.setStatus(NodeStatus.EXECUTING);
        node.getApplicationLayer().send(message.getSender(), new ReceivedTokenMessage(node));
    }

    /**
     * Текущий узел получил запрос по предоставлению всей информации о текущем узле
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

    }

    public void handleDoViewChange() {

    }

    public void handleDiscardViewChange(DiscardViewChangeMessage message) {

    }

    public void handleRecovery(RecoveryMessage message) {

    }

    public void handleRecoveryResponse(RecoveryResponseMessage message) {

    }
}
