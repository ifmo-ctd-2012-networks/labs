package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.Message;
import ru.ifmo.info.MessageParseException;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramReceiver;
import ru.ifmo.threads.ClosableRunnable;
import ru.ifmo.threads.ClosingListener;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BroadcastAnnouncer implements AutoCloseable {
    /**
     * Which number of missing packets in a row is enough for considering node as lost
     */
    public static final int LOST_THRESHOLD = 5;
    public static final int SENDING_THREAD_NUM = 2;

    private final Logger logger = Logger.getLogger(BroadcastAnnouncer.class);

    private int port;
    private long sendDelay;

    protected Map<NodeInfo, Integer> missedPacketsCounter = new ConcurrentSkipListMap<>();

    private final ScheduledExecutorService executor;

    private List<ClosableRunnable> servants;

    protected ClosingListener closingListener = new ClosingListener();

    public BroadcastAnnouncer(int port, long sendDelay) throws IOException {
        this.port = port;
        this.sendDelay = sendDelay;

        closingListener.register(() -> logger.info("Closing"));

        this.servants = Arrays.asList(
                new Receiver(),
                new MissedPacketsListUpdater()
        );
        executor = Executors.newScheduledThreadPool(servants.size() + SENDING_THREAD_NUM);
    }


    public interface Waiter {
        void await() throws SocketException;
    }

    public Waiter start() throws SocketException {
        List<Future<?>> futures = servants.stream()
                .map(Thread::new)
                .map(executor::submit)
                .collect(Collectors.toList());

        return () -> {
            for (Iterator<Future<?>> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future<?> future = iterator.next();
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.warn("Unexpected exception", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    iterator.forEachRemaining((f -> f.cancel(true)));
                }
            }
        };
    }

    public interface Task extends AutoCloseable {
        void invoke(Map<NodeInfo, Integer> missedPacketCounter);
    }

    public interface TaskCreator {
        Task create(int port) throws IOException;
    }

    public Runnable addRepeatingTask(TaskCreator taskCreator) throws IOException {
        Task task = taskCreator.create(port);
        Runnable invoke = () -> {
            try {
                task.invoke(missedPacketsCounter);
            } catch (Exception e) {
                logger.warn("Exception while performing task", e);
            }
        };
        closingListener.register(task);
        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(invoke, 0, sendDelay, TimeUnit.MILLISECONDS);
        return () -> scheduledFuture.cancel(false);
    }

    public void close() {
        closingListener.closeAll();
    }

    protected void onMissedPacketNumUpdate(NodeInfo info, int missed) {
    }

    /**
     * Gets broadcasts from other users
     */
    private class Receiver extends DatagramReceiver {
        protected Receiver() throws SocketException {
            super(port);
            closingListener.register(this);
        }

        protected void onReceive(byte[] bytes, int length) throws IOException {
            try {
                Message message = new Message(bytes, length);

                logger.info(String.format("Received %s", message));
                missedPacketsCounter.put(message.getNodeInfo(), -1);
            } catch (MessageParseException e) {
                logger.trace(String.format("Received incorrect message (%s)", e.getMessage()));
            }
        }

        @Override
        public void close() {
            super.close();
            closingListener.closeAll();
        }
    }

    /**
     * Periodically checks for missed
     */
    private class MissedPacketsListUpdater implements ClosableRunnable {
        public MissedPacketsListUpdater() {
            closingListener.register(this);
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(sendDelay);
                    updateList();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void updateList() {
            logger.info("List of active announcers:");
            missedPacketsCounter.forEach((info, value) -> {
                int newVal = value + 1;
                logger.info(String.format("   :: %s (%d missed%s)", info, newVal, newVal >= LOST_THRESHOLD ? "!!" : ""));

                if (newVal >= LOST_THRESHOLD) {
                    missedPacketsCounter.remove(info);
                } else {
                    missedPacketsCounter.put(info, newVal);
                }

                onMissedPacketNumUpdate(info, newVal);
            });
        }

        @Override
        public void close() {
            closingListener.closeAll();
        }
    }
}
