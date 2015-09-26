package ru.ifmo.ctddev.varlamov;


import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

public class Logger implements Runnable {

    private final BlockingQueue<String> queue;

    private final PrintStream out;

    public Logger(PrintStream out, BlockingQueue<String> queue) {
        this.out = out;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            while (!queue.isEmpty()) {
                out.println(queue.poll());
            }
        }
    }

    public void log(String s) {
        queue.offer(s);
    }
}