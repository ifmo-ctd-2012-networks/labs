package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class DoViewChangeMessage extends Message {
    public static final String TYPE = "doViewChange";

    public DoViewChangeMessage(Node sender) {
        super(sender, TYPE);
    }

    public DoViewChangeMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleDoViewChange(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder().build();
    }
}
