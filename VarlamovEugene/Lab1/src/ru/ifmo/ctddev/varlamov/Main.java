package ru.ifmo.ctddev.varlamov;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static final int PORT = 8888;
    private static final int MAX_QUEUE_MESSAGES = 1000;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(MAX_QUEUE_MESSAGES);
        //executorService.execute(new Server("valid host", PORT));
        executorService.execute(new CrashServer(PORT));
        Logger logger = new Logger(System.out, queue);
        executorService.execute(logger);
        executorService.execute(new Client(logger, PORT));
    }
}