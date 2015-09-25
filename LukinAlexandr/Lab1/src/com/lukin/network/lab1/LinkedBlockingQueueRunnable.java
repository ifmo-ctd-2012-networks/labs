package com.lukin.network.lab1;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Саша on 18.09.2015.
 */
public abstract class LinkedBlockingQueueRunnable<T> implements Runnable {
    private LinkedBlockingQueue<T>  requests = new LinkedBlockingQueue<T>();

    public void run() {
        while (!Thread.interrupted()){
            try {
                execute(requests.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void put(T t) throws InterruptedException {
        requests.put(t);
    }

    protected abstract void execute(T t);
}
