package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class ReceivedTokenMessage extends Message {
    public static final String TYPE = "receivedToken";
    public static final String TOKEN = "token";
    private final String token;

    public ReceivedTokenMessage(Node sender, String token) {
        super(sender, TYPE);
        this.token = token;
    }

    public ReceivedTokenMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
        this.token = content.getString(TOKEN);
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleReceivedToken(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder()
                .add(TOKEN, token)
                .build();
    }
}
