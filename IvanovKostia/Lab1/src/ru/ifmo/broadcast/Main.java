package ru.ifmo.broadcast;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        String networkInterfaceName = args[0];
        int port = Integer.parseInt(args[1]);

        NetworkInterface network = Optional.ofNullable(NetworkInterface.getByName(networkInterfaceName))
                .orElseThrow(() -> new IllegalArgumentException("No such network found"));

        Thread stopWaiter = null;
        try {
            BroadcastAnnouncer announcer = new BroadcastAnnouncer(port, 5_000L, network);
            stopWaiter = new Thread(new StdinWaiter(announcer::close));
            stopWaiter.start();

            announcer.run();
            announcer.close();
        } finally {
            Optional.ofNullable(stopWaiter)
                    .ifPresent(Thread::interrupt);
        }

    }

    private static class StdinWaiter implements Runnable {
        private final Runnable command;

        public StdinWaiter(Runnable command) {
            this.command = command;
        }

        @Override
        public void run() {
            try {
                System.in.read();
                command.run();
            } catch (IOException ignored) {
            }
        }
    }
}
