package ru.network.state;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.ServerNode;
import ru.network.message.ReceivedTokenMessage;
import ru.network.message.SendTokenMessage;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author victor
 */
public class ExecutingState extends State {
    private final Logger log = LoggerFactory.getLogger(ExecutingState.class);
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final long RECIEVED_TIMEOUT = 5000;
    private boolean isReceivedTimeout = false;
    private final Runnable computing = () -> {
        // compute PI
        String newData = "";
        node.setData(newData);
        node.setOperationNumber(node.getOperationNumber() + 1);
        looper.add(() -> {
            node.getApplicationLayer().send(null, new SendTokenMessage(node, node.getOperationNumber(), node.getToken(), node.getData()));
        });
    };

    public ExecutingState(ServerNode node) {
        super(node);
    }

    @Override
    public void enter() {
        log.debug("enter");
        executor.execute(computing);
    }

    @Override
    public void leave() {
        log.debug("leave");
    }

    @Override
    public void handleReceivedToken(ReceivedTokenMessage message) {
        if (!isReceivedTimeout) {

        }
    }
}
