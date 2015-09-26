package ru.ifmo.threads;

import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClosingListener {
    private static final Logger logger = Logger.getLogger(ClosingListener.class);

    private final ConcurrentLinkedQueue<AutoCloseable> toClose = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean closed = new AtomicBoolean();

    public void register(AutoCloseable closeable) {
        toClose.add(closeable);

        if (closed.get()) {
            closeAll();
        }
    }

    public void closeAll() {
        if (!closed.compareAndSet(false, true)) return;

        while (true) {
            AutoCloseable closeable = toClose.poll();
            if (closeable == null) return;

            try {
                closeable.close();
            } catch (Exception e) {
                logger.info("Instance has been closed with exception", e);
            }
        }
    }
}
