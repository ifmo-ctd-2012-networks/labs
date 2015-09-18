package com.khovanskiy.network.lab1;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author victor
 */
public abstract class QueueRunnable<T> implements Runnable {
    private final LinkedBlockingQueue<T> requests = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                handle(requests.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void add(T element) throws InterruptedException {
        requests.put(element);
    }

    protected abstract void handle(T element);
}
