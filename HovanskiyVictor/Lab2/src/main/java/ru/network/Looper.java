package ru.network;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author victor
 */
public class Looper {

    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
    private static Looper sMainLooper;  // guarded by Looper.class

    final LinkedBlockingQueue<Runnable> mQueue;
    final Thread mThread;

    private Looper(boolean quitAllowed) {
        mQueue = new LinkedBlockingQueue<>();
        mThread = Thread.currentThread();
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }

    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper();
        }
    }

    public static Looper getMainLooper() {
        synchronized (Looper.class) {
            return sMainLooper;
        }
    }

    public static void loop() {
        final Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        while (true) {
            try {
                Runnable runnable = me.mQueue.take();
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    public static LinkedBlockingQueue<Runnable> myQueue() {
        return myLooper().mQueue;
    }

    public static void prepare() {
        prepare(true);
    }

    public void add(Runnable runnable) {
        mQueue.add(runnable);
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == mThread;
    }
}
