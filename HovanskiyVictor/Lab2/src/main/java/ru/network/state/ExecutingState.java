package ru.network.state;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.Node;
import ru.network.ServerNode;
import ru.network.message.ReceivedTokenMessage;
import ru.network.message.SendTokenMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author victor
 */
public class ExecutingState extends State {
    private final Logger log = LoggerFactory.getLogger(ExecutingState.class);
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final long RECIEVED_TIMEOUT = 5000;
    private long previous;
    private AtomicBoolean updated = new AtomicBoolean(false);
    private volatile String dataStamp;
    private boolean listenReceivedToken;
    private ReceivedTokenMessage receivedTokenMessage;

    public ExecutingState(ServerNode node) {
        super(node);
    }

    @Override
    public void tick() {
        super.tick();
        long timestamp = System.currentTimeMillis();
        if (updated.compareAndSet(true, false)) {
            node.setData(dataStamp);
            node.setOperationNumber(node.getOperationNumber() + 1);
            Node next = node.getRing().right();
            if (next == null) {
                log.debug("Токен некому передавать, остается у нас");
                node.setState(new ExecutingState(node));
            } else {
                log.debug("Передаем токен дальше " + next);
                listenReceivedToken = true;
                previous = System.currentTimeMillis();
                node.getApplicationLayer().send(next, new SendTokenMessage(node, node.getOperationNumber(), node.getToken(), node.getData()));
            }
        } else if (listenReceivedToken && timestamp - previous >= RECIEVED_TIMEOUT) {
            log.debug("Время ожидания " + RECIEVED_TIMEOUT + "ms истекло");
            listenReceivedToken = false;
            log.debug("Токен не удалось передать");
            node.setState(new ExecutingState(node));
        }
    }

    @Override
    public void enter() {
        log.debug("enter");
        dataStamp = node.getData();
        executor.execute(() -> {
            log.debug("Вычисляем следующие 20 цифр числа PI...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dataStamp = dataStamp + "0";
            log.debug("Готово");
            updated.set(true);
        });
    }

    @Override
    public void handleSendToken(SendTokenMessage message) {
        assert message.getSender().isValid();
        if (!listenReceivedToken || node.getMacAddress().compareTo(message.getSender().getMacAddress()) < 0) {
            if (node.getOperationNumber() < message.getOperationNumber()) {
                node.setOperationNumber(message.getOperationNumber());
                node.setToken(message.getToken());
                node.setData(message.getData());
            }
            node.getApplicationLayer().send(message.getSender(), new ReceivedTokenMessage(node, message.getToken()));
        }
    }

    @Override
    public void leave() {
        log.debug("leave");
    }

    @Override
    public void handleReceivedToken(ReceivedTokenMessage message) {
        if (listenReceivedToken) {
            assert receivedTokenMessage == null;
            receivedTokenMessage = message;
            log.debug("Токен удалось передать");
            node.setToken(null);
            node.setState(new NormalState(node));
        }
    }
}
