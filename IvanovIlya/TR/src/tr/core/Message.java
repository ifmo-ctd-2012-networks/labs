package tr.core;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class Message {
    public static final int TOKEN_PASS = 0;
    public static final int TOKEN_RECEIVE = 1;
    public static final int CHECK_STATE = 2;
    public static final int CHECK_RESPONSE = 3;

    private int type;
    private Token token;
    private boolean forward;
    private Object payload;

    public Message(int type) {
        this.type = type;
    }

    public Message(int type, Token token) {
        this.type = type;
        this.token = token;
    }

    public Message(int type, Token token, boolean forward, Object payload) {
        this.type = type;
        this.token = token;
        this.forward = forward;
        this.payload = payload;
    }

    public static Message readMessage(Scanner sc) {
        Message message = new Message(sc.nextInt());
        if (message.type < 2) {
            byte[] mac = new byte[6];
            for (int i = 0; i < 6; i++) {
                mac[i] = sc.nextByte();
            }
            message.token = new Token(mac, sc.nextInt());
            if (message.type == 0) {
                message.forward = sc.nextBoolean();
                message.payload = Configuration.manager.readPayload(sc);
            }
        }
        return message;
    }

    public void writeMessage(Writer writer) throws IOException {
        writer.write(type + "\n");
        if (type < 2) {
            for (int i = 0; i < token.getMac().length; i++) {
                writer.write(token.getMac()[i] + "\n");
            }
            writer.write(token.getVersion() + "\n");
            if (type == 0) {
                writer.write(forward + "\n");
                Configuration.manager.writePayload(payload, writer);
            }
        }
    }

    public int getType() {
        return type;
    }

    public Token getToken() {
        return token;
    }

    public boolean getForward() {
        return forward;
    }

    public Object getPayload() {
        return payload;
    }
}
