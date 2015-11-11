package token.ring.message;

import sender.RequestMessage;
import sender.message.VoidMessage;
import token.ring.Priority;

public class AmCandidateMsg extends RequestMessage<VoidMessage> {
    public final Priority priority;

    public AmCandidateMsg(Priority priority) {
        this.priority = priority;
    }
}
