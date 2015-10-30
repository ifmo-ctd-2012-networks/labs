package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class RecoveryMessage extends Message {
    public static final String TYPE = "recovery";

    public RecoveryMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleRecovery(this);
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
