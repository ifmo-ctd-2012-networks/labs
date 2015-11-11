package token.ring;

import sender.MessageSender;

public class Context {
    MessageSender sender;
    private Priority priority;

    void switchToState(Object state){
        // freezes sender
        // closes current state
        // logs about state switching
        // stores and starts specified state
        // unfreezes sender
    }

    public Priority getPriority() {
        return priority;
    }
}
