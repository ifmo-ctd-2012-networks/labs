package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class RecoveryMessage extends Message {
    public static final String TYPE = "recovery";
    public static final String TIMESTAMP = "timestamp";

    private final long timestamp;

    public RecoveryMessage(Node sender, long timestamp) {
        super(sender, TYPE);
        this.timestamp = timestamp;
    }

    public RecoveryMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
        this.timestamp = content.getJsonNumber(TIMESTAMP).longValue();
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleRecovery(this);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder()
                .add(TIMESTAMP, timestamp)
                .build();
    }
}
