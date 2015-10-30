package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class RecoveryResponseMessage extends Message {
    public static final String TYPE = "recoveryResponse";

    public RecoveryResponseMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleRecoveryResponse(this);
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
