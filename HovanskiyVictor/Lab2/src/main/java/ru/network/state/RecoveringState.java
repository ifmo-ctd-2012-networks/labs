package ru.network.state;

import ru.network.Node;
import ru.network.message.SendTokenMessage;

/**
 * @author victor
 */
public class RecoveringState extends State {

    public RecoveringState(Node node) {
        super(node);
    }

    @Override
    public void handleSendToken(SendTokenMessage message) {
        super.handleSendToken(message);
    }
}
