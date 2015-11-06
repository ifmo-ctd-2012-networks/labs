package tr.core;

import javafx.util.Pair;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class StateChecker {
    private final Set<InetAddress> bad = new HashSet<>();
    private final Checker checker = new Checker();
    private final MessageProcessor processor = new MessageProcessor();
    private volatile ConnectionManager manager;
    private volatile boolean isFinished;

    public void setManager(ConnectionManager manager) {
        this.manager = manager;
    }

    public void start() {
        checker.start();
        processor.start();
    }

    public void process(InetAddress address, Message message) {
        processor.process(address, message);
    }

    public void add(InetAddress address) {
        synchronized (bad) {
            bad.add(address);
        }
    }

    public boolean contains(InetAddress address) {
        synchronized (bad) {
            return bad.contains(address);
        }
    }

    public void finish() {
        isFinished = true;
        checker.interrupt();
        processor.interrupt();
    }

    private class Checker extends Thread {
        @Override
        public void run() {
            while (!isFinished) {
                synchronized (bad) {
                    for (InetAddress address : bad) {
                        manager.send(address, new Message(Message.CHECK_STATE));
                    }
                }
                long time = System.currentTimeMillis();
                while (System.currentTimeMillis() < time + Configuration.CHECKER_TIMEOUT) {
                    try {
                        Thread.sleep(time + Configuration.CHECKER_TIMEOUT - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }

    private class MessageProcessor extends Thread {
        private final Queue<Pair<InetAddress, Message>> queue = new ArrayDeque<>();

        @Override
        public void run() {
            while (!isFinished) {
                Pair<InetAddress, Message> message;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    message = queue.poll();
                }
                if (message.getValue().getType() == Message.CHECK_STATE) {
                    manager.send(message.getKey(), new Message(Message.CHECK_RESPONSE));
                } else {
                    synchronized (bad) {
                        bad.remove(message.getKey());
                    }
                }
            }
        }

        public void process(InetAddress address, Message message) {
            synchronized (queue) {
                queue.add(new Pair<>(address, message));
                queue.notify();
            }
        }
    }
}
