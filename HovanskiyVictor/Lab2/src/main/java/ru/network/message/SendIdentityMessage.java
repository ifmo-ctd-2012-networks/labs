package ru.network.message;

import ru.network.Node;
import ru.network.ServerNode;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author victor
 */
public class SendIdentityMessage extends Message {
    public static final String TYPE = "sendIdentity";
    public static final String HOSTNAME = "hostname";
    public static final String PORT = "port";
    public static final String MAC_ADDRESS = "macAddress";

    private final String hostname;
    private final int port;
    private final String macAddress;

    public SendIdentityMessage(Node sender, String hostname, String macAddress, int port) {
        super(sender, TYPE);
        this.hostname = hostname;
        this.macAddress = macAddress;
        this.port = port;
    }

    public SendIdentityMessage(Node sender, JsonObject content) {
        super(sender, TYPE);
        this.hostname = content.getString(HOSTNAME);
        this.port = content.getInt(PORT);
        this.macAddress = content.getString(MAC_ADDRESS);
    }

    public String getHostname() {
        return hostname;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void delegate(ServerNode node) {
        node.getState().handleSendIdentity(this);
    }

    @Override
    protected JsonObject encode() {
        return Json.createObjectBuilder()
                .add(HOSTNAME, hostname)
                .add(PORT, port)
                .add(MAC_ADDRESS, macAddress)
                .build();
    }
}
