package ru.network.message;

import ru.network.Node;

import javax.json.JsonObject;

/**
 * @author victor
 */
public class DoViewChangeMessage extends Message {
    public static final String TYPE = "doViewChange";

    public DoViewChangeMessage(Node sender) {
        super(sender, TYPE);
    }

    @Override
    protected JsonObject encode() {
        return null;
    }
}
