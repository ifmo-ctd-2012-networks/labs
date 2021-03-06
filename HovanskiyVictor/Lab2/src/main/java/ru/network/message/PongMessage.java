package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victors
 */
public class PongMessage extends Message {
    public static final String TYPE = "pong";

    public PongMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handlePong(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder().build();
    }
}
