package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class PingMessage extends Message {
    public static final String TYPE = "ping";

    public PingMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handlePing(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder().build();
    }
}
