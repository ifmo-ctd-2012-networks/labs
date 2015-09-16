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

public class BroadcastAnnouncer {
    /**
     * Which number of missing packets in a row is enough for considering node as lost
     */
    private static final int LOST_THRESHOLD = 5;

    private final Logger logger = Logger.getLogger(BroadcastAnnouncer.class);

    private int port;
    private long sendDelay;

    private final NodeInfo myNodeInfo;

    private boolean isClosed = false;

    private Map<NodeInfo, Integer> missedPacketsCounter = new ConcurrentSkipListMap<>();

    private Thread[] threads = new Thread[3];

    public BroadcastAnnouncer(int port, long sendDelay) throws IOException {
        this(port, sendDelay, NodeInfo.makeLocal());
    }

    public BroadcastAnnouncer(int port, long sendDelay, NodeInfo info) throws IOException {
        this.port = port;
        this.sendDelay = sendDelay;
        this.myNodeInfo = info;
    }

    public BroadcastAnnouncer start() throws SocketException {
        // receiver thread
        threads[0] = new Thread(new DatagramReceiver(port){
            protected void onReceive(byte[] bytes, int length) throws IOException {
                Message message = new Message(bytes);
                logger.info(String.format("[%s] - Received %s", myNodeInfo, message));

                missedPacketsCounter.put(message.getNodeInfo(), -1);
            }
        });

        // sender thread
        threads[1] = new Thread(new DatagramSender(port, sendDelay){
            protected void send() throws IOException {
                if (Math.random() < 2. / 3.) return;

                NodeInfo info = myNodeInfo;
                Message message = info.toMessage();
                sendBytes(message.toBytes());
                logger.info(String.format("[%s] - Broadcast %s", info, message));
            }
        });

        // list updater thread
        threads[2] = new Thread(new MissedPacketsListUpdater());

        for (Thread thread : threads) {
            thread.start();
        }

        return this;
    }


    public void close() {
        if (isClosed) return;
        isClosed = true;

        for (Thread thread : threads) {
            thread.interrupt();

            // join even if this thread get interrupted in process
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
                    logger.info(String.format("[%s] - %s have not been responding for too long", myNodeInfo, info));
                    missedPacketsCounter.remove(info);
                } else {
                    missedPacketsCounter.put(info, newVal);
                }
            });
        }
    }
}
