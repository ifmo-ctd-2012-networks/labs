package ru.network.state;

import ru.network.Node;
import ru.network.NodeStatus;
import ru.network.message.PingMessage;
import ru.network.message.PongMessage;
import ru.network.message.ReceivedTokenMessage;
import ru.network.message.SendTokenMessage;

/**
 * @author victor
 */
public abstract class State {

    protected final Node node;

    public State(Node node) {
        this.node = node;
    }

    /**
     * Получили пинг-сообщение, обязаны на него ответить
     * @param message пинг-сообщение
     */
    public void handlePing(PingMessage message) {
        node.getWrapper().send(message.getSender(), new PongMessage());
    }

    /**
     * Обновляем узел, пославший нам сообщение, как активный
     * @param message ответ на пинг
     */
    public void handlePong(PongMessage message) {
        node.getWrapper().update(message.getSender());
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
        node.getWrapper().send(message.getSender(), new ReceivedTokenMessage());
    }

    public void handleReceivedToken(ReceivedTokenMessage message) {

    }
}
