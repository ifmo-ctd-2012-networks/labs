package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class RecoveryResponseMessage extends Message {
    public static final String TYPE = "recoveryResponse";
    public static final String OPERATION_NUMBER = "opNumber";
    public static final String DATA = "data";
    public static final String TIMESTAMP = "timestamp";
    private final long operationNumber;
    private final String data;
    private final long timestamp;

    public RecoveryResponseMessage(Node sender, long operationNumber, String data, long timestamp) {
        super(sender, TYPE);
        this.operationNumber = operationNumber;
        this.data = data;
        this.timestamp = timestamp;
    }

    public RecoveryResponseMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
        this.operationNumber = content.getJsonNumber(OPERATION_NUMBER).longValue();
        this.data = content.getString(DATA);
        this.timestamp = content.getJsonNumber(TIMESTAMP).longValue();
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleRecoveryResponse(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder()
                .add(OPERATION_NUMBER, operationNumber)
                .add(DATA, data)
                .add(TIMESTAMP, timestamp)
                .build();
    }

    public long getOperationNumber() {
        return operationNumber;
    }

    public String getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
