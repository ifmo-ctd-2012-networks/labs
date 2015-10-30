package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class GetIdentityMessage extends Message {
    public static final String TYPE = "getIdentity";

    public GetIdentityMessage(ServerNode sender) {
        super(sender, TYPE);
    }

    public GetIdentityMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleGetIdentity(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder().build();
    }
}
