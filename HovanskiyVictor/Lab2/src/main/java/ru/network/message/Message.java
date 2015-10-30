package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.StringReader;

/**
 * @author victor
 */
public abstract class Message {
    private Node sender;

    public Node getSender() {
        return sender;
    }

    private static final String MESSAGE_TYPE = "type";
    private static final String MESSAGE_CONTENT = "content";

    private final String type;

    public Message(Node sender, String type) {
        this.sender = sender;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static JsonObject encode(Message message) {
        return Json.createObjectBuilder()
                .add(Message.MESSAGE_TYPE, message.getType())
                .add(Message.MESSAGE_CONTENT, message.encode())
                .build();
    }

    public static Message decode(Node sender, String jsonString) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject jsonObject = jsonReader.readObject();
        return decode(sender, jsonObject);
    }

    public void delegate(ServerNode node) {

    }

    public static Message decode(Node sender, JsonObject jsonObject) {
        JsonString type = jsonObject.getJsonString(Message.MESSAGE_TYPE);
        JsonObject content = jsonObject.getJsonObject(Message.MESSAGE_CONTENT);

        switch (type.getString()) {
            case GetIdentityMessage.TYPE:
                return new GetIdentityMessage(sender, content);
            case SendIdentityMessage.TYPE:
                return new SendIdentityMessage(sender, content);
        }
        throw new IllegalArgumentException("Unknown message type: \"" + type + "\"");
    }

    protected abstract JsonObject encode();

    @Override
    public String toString() {
        return getType() + ":" + encode().toString();
    }
}
