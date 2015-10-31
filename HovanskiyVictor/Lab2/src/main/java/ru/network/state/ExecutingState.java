package ru.network.state;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.CalculationService;
import ru.network.Node;
import ru.network.NodeStatus;
import ru.network.ServerNode;
import ru.network.message.ReceivedTokenMessage;
import ru.network.message.RecoveryMessage;
import ru.network.message.RecoveryResponseMessage;
import ru.network.message.SendTokenMessage;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author victor
 */
public class ExecutingState extends State {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final long RECEIVED_TIMEOUT = 5000;
    private static final int NUMBERS_COUNT = 5;
    private final Logger log = LoggerFactory.getLogger(ExecutingState.class);
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
            log.info("PI = " + node.getData());
            node.setOperationNumber(node.getOperationNumber() + 1);
            List<Node> neighbours = node.getRing().neighbours();
            boolean resend = false;
            for (Node neighbour : neighbours) {
                if (neighbour.isActive()) {
                    log.debug("Передаем токен дальше " + neighbour);
                    listenReceivedToken = true;
                    previous = System.currentTimeMillis();
                    node.getApplicationLayer().send(neighbour, new SendTokenMessage(node, node.getOperationNumber(), node.getToken(), node.getData()));
                    resend = true;
                    break;
                }
            }
            if (!resend) {
                log.debug("Токен некому передавать, остается у нас");
                node.setState(new ExecutingState(node));
            }
        } else if (listenReceivedToken && timestamp - previous >= RECEIVED_TIMEOUT) {
            log.debug("Время ожидания " + RECEIVED_TIMEOUT + "ms истекло");
            listenReceivedToken = false;
            log.debug("Токен не удалось передать");
            node.setState(new ExecutingState(node));
        }
    }

    @Override
    public void enter() {
        log.debug("enter");
        dataStamp = node.getData();
        assert dataStamp != null;
        executor.execute(() -> {
            String temp = dataStamp;
            log.debug("Вычисляем следующие " + NUMBERS_COUNT + " цифр числа PI...");
            temp += CalculationService.instance().nextNumbers(temp.length(), NUMBERS_COUNT);
            log.debug("Готово");
            dataStamp = temp;
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
    public NodeStatus getStatus() {
        return NodeStatus.EXECUTING;
    }

    @Override
    public void handleReceivedToken(ReceivedTokenMessage message) {
        if (listenReceivedToken) {
            if (message.getToken().equals(node.getToken())) {
                assert receivedTokenMessage == null;
                receivedTokenMessage = message;
                log.debug("Токен удалось передать");
                node.setToken(null);
                node.setState(new NormalState(node));
            }
        }
    }
}
