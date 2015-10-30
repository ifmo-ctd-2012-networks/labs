package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class SendTokenMessage extends Message {
    private static final String TYPE = "sendToken";
    private final String token;
    private final String data;
    private final long operationNumber;

    public SendTokenMessage(Node sender, long operationNumber, String token, String data) {
        super(sender, TYPE);
        this.operationNumber = operationNumber;
        this.token = token;
        this.data = data;
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleSendToken(this);
    }

    public long getOperationNumber() {
        return operationNumber;
    }

    public String getToken() {
        return token;
    }

    public String getData() {
        return data;
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
