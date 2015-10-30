package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class DiscardViewChangeMessage extends Message {

    public static final String TYPE = "discardViewChange";

    public DiscardViewChangeMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleDiscardViewChange(this);
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
