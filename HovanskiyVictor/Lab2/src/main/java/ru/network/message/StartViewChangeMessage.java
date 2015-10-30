package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class StartViewChangeMessage extends Message {
    private static final String TYPE = "startViewChange";
    private final long operationNumber;

    public StartViewChangeMessage(Node sender, long operationNumber) {
        super(sender, TYPE);
        this.operationNumber = operationNumber;
    }

    public long getOperationNumber() {
        return operationNumber;
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleStartViewChange(this);
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
