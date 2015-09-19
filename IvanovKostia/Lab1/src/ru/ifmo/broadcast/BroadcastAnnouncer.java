package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.Message;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramReceiver;
import ru.ifmo.network.DatagramSender;
import ru.ifmo.threads.ClosableRunnable;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BroadcastAnnouncer implements AutoCloseable {
    /**
     * Which number of missing packets in a row is enough for considering node as lost
     */
    private static final int LOST_THRESHOLD = 5;

    private final Logger logger = Logger.getLogger(BroadcastAnnouncer.class);

    private int port;
    private long sendDelay;
    private final NodeInfo myNodeInfo;

    private final AtomicBoolean closed = new AtomicBoolean();

    private Map<NodeInfo, Integer> missedPacketsCounter = new ConcurrentSkipListMap<>();

    private final ExecutorService executor;

    private List<ClosableRunnable> servants;

    public BroadcastAnnouncer(int port, long sendDelay, NetworkInterface network) throws IOException {
        this.port = port;
        this.sendDelay = sendDelay;
        this.myNodeInfo = Optional.ofNullable(NodeInfo.atNetworkInterface(network))
                .orElseThrow(() -> new IllegalArgumentException("Cannot launch at network interface " + network));

        this.servants = Arrays.asList(
                new Sender(network),
                new Receiver(),
                new MissedPacketsListUpdater()
        );
        executor = Executors.newFixedThreadPool(servants.size());
    }

    public void run() throws SocketException {
        List<Future<?>> futures = start();


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
    }

    public List<Future<?>> start() throws SocketException {
        return servants.stream()
                .map(Thread::new)
                .map(executor::submit)
                .collect(Collectors.toList());
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.info("Closing");
            executor.shutdownNow();

            for (ClosableRunnable servant : servants) {
                try {
                    servant.close();
                } catch (Exception ignore) {
                }
            }
        }
    }


    private class Sender extends DatagramSender {
        public Sender(NetworkInterface networkInterface) throws SocketException {
            super(port, sendDelay, networkInterface);
        }

        protected void send() throws IOException {
            NodeInfo info = myNodeInfo;
            Message message = info.toMessage();
            logger.info(String.format("[%s] - Broadcast %s", info, message));
            sendBytes(message.toBytes());
        }

        @Override
        public void close() {
            super.close();
            BroadcastAnnouncer.this.close();
        }
    }

    private class Receiver extends DatagramReceiver {
        protected Receiver() throws SocketException {
            super(port);
        }

        protected void onReceive(byte[] bytes, int length) throws IOException {
            Message message = new Message(bytes);
            logger.info(String.format("[%s] - Received %s", myNodeInfo, message));

            missedPacketsCounter.put(message.getNodeInfo(), -1);
        }

        @Override
        public void close() {
            super.close();
            BroadcastAnnouncer.this.close();
        }
    }

    private class MissedPacketsListUpdater implements ClosableRunnable {
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
            logger.info(String.format("[%s] - List of active announcers:", myNodeInfo));
            missedPacketsCounter.forEach((info, value) -> {
                int newVal = value + 1;
                logger.info(String.format("[%s] -    :: %s (%d missed%s)", myNodeInfo, info, newVal, newVal >= LOST_THRESHOLD ? "!!" : ""));

                if (newVal >= LOST_THRESHOLD) {
                    missedPacketsCounter.remove(info);
                } else {
                    missedPacketsCounter.put(info, newVal);
                }
            });
        }

        @Override
        public void close() {
            BroadcastAnnouncer.this.close();
        }
    }
}
