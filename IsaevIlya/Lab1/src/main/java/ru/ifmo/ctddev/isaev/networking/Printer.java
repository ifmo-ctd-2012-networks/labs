package ru.ifmo.ctddev.isaev.networking;

import java.util.HashSet;
import java.util.Set;

import static ru.ifmo.ctddev.isaev.networking.Main.*;

/**
 * @author Ilya Isaev
 */
public class Printer implements Runnable {

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (broadcasters) {
                    synchronized (pendingMessages) {//no modification allowed
                        broadcasters.forEach((k, v) -> ++v.skippedAnnounces);
                        pendingMessages.forEach((k, v) -> --broadcasters.get(k).skippedAnnounces);
                        pendingMessages.clear();
                        Set<Long> toRemove = new HashSet<>();
                        broadcasters.keySet().stream()
                                .filter(key -> broadcasters.get(key).skippedAnnounces >= 5)
                                .forEach((s) -> {
                                    BroadcasterInfo info = broadcasters.get(s);
                                    System.out.format("Removed broadcaster with mac: %d, hostname = %s because of 5 missed announces\n",
                                            info.mac, info.hostname);
                                    toRemove.add(s);
                                });
                        toRemove.forEach(broadcasters::remove);
                        pendingMessages.clear();
                        printBroadcasters();
                    }
                }
                Thread.sleep(SLEEP_TIME);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printBroadcasters() {
        System.out.println("Current broadcasters: ");
        for (BroadcasterInfo info : broadcasters.values()) {
            System.out.format("| mac: %d, hostname: \"%s\"\n",
                    info.mac, info.hostname);
        }
        System.out.println("________________________");
    }
}
