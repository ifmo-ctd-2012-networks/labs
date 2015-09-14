package ru.ifmo.broadcast;

import org.apache.log4j.Logger;
import ru.ifmo.info.Message;
import ru.ifmo.info.NodeInfo;
import ru.ifmo.network.DatagramReceiver;
import ru.ifmo.network.DatagramSender;

import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcastAnnouncer {
    /**
     * Which number of missing packets in a row is enough for considering node as lost
     */
    private static final int LOST_THRESHOLD = 5;
    private static final int SERVANTS_NUM = 3;

    private final Logger logger = Logger.getLogger(BroadcastAnnouncer.class);

    private int port;
    private long sendDelay;

    private ExecutorService executor = Executors.newFixedThreadPool(SERVANTS_NUM);
    private boolean isClosed = false;

    private Map<NodeInfo, Integer> missedPacketsCounter = new ConcurrentSkipListMap<>();

    public BroadcastAnnouncer(int port, long sendDelay) {
        this.port = port;
        this.sendDelay = sendDelay;
    }

    public BroadcastAnnouncer start() throws SocketException {
        // sender thread
        executor.submit(new DatagramSender(port, sendDelay){
            protected void send() throws IOException {
                if (Math.random() < 2. / 3.) return;

                NodeInfo info = myNodeInfo();
                Message message = info.toMessage();
                sendBytes(message.toBytes());
                logger.info(String.format("[%s] Broadcast %s", info, message));
            }
        });

        // receiver thread
        executor.submit(new DatagramReceiver(port){
            protected void onReceive(byte[] bytes, int length) throws IOException {
                Message message = new Message(bytes);
                logger.info(String.format("[%s] Received %s", myNodeInfo(), message));

                missedPacketsCounter.put(message.getNodeInfo(), -1);
            }
        });

        // list updater thread
        executor.submit(new MissedPacketsListUpdater());

        return this;
    }

    protected NodeInfo myNodeInfo() throws IOException {
        return NodeInfo.makeLocal();
    }

    public void close() {
        if (isClosed) return;

        isClosed = true;
        executor.shutdown();
    }

    private class MissedPacketsListUpdater implements Runnable {
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
            missedPacketsCounter.forEach((info, value) -> {
                int newVal = value + 1;
                if (newVal >= LOST_THRESHOLD) {
                    logger.info(String.format("Node %s have been not responding for too long", info));
                    missedPacketsCounter.remove(info);
                } else {
                    missedPacketsCounter.put(info, newVal);
                }
            });
        }
    }
}
