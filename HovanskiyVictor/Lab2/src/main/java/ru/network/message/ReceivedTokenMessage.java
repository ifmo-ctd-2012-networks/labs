package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class ReceivedTokenMessage extends Message {
    public static final String TYPE = "receivedToken";

    public ReceivedTokenMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleReceivedToken(this);
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
