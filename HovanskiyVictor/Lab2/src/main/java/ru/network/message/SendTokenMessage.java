package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class SendTokenMessage extends Message {
    public static final String TYPE = "sendToken";
    public static final String OPERATION_NUMBER = "opNumber";
    public static final String TOKEN = "token";
    public static final String DATA = "data";
    private final String token;
    private final String data;
    private final long operationNumber;

    public SendTokenMessage(Node sender, long operationNumber, String token, String data) {
        super(sender, TYPE);
        this.operationNumber = operationNumber;
        this.token = token;
        this.data = data;
    }

    public SendTokenMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
        this.operationNumber = content.getJsonNumber(OPERATION_NUMBER).longValue();
        this.token = content.getString(TOKEN);
        this.data = content.getString(DATA);
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
        return Json.createObjectBuilder()
                .add(OPERATION_NUMBER, operationNumber)
                .add(TOKEN, token)
                .add(DATA, data)
                .build();
    }
}
