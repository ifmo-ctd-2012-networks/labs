package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class StartViewChangeMessage extends Message {
    public static final String TYPE = "startViewChange";
    public static final String OPERATION_NUMBER = "opNumber";
    private final long operationNumber;

    public StartViewChangeMessage(Node sender, long operationNumber) {
        super(sender, TYPE);
        this.operationNumber = operationNumber;
    }

    public StartViewChangeMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
        this.operationNumber = content.getJsonNumber(OPERATION_NUMBER).longValue();
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
        return Json.createObjectBuilder()
                .add(OPERATION_NUMBER, operationNumber)
                .build();
    }
}
