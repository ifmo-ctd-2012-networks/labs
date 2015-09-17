package ru.ifmo.broadcast;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BroadcastAnnouncer announcer = new BroadcastAnnouncer(34522, 1_000L)
                .start();

        //noinspection ResultOfMethodCallIgnored
        System.in.read();

        announcer.close();

    }
}
