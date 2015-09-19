package ru.ifmo.ctddev.isaev.networking;

import dnl.utils.text.table.TextTable;

import java.util.HashSet;
import java.util.Set;

import static ru.ifmo.ctddev.isaev.networking.Main.*;

/**
 * @author Ilya Isaev
 */
public class Printer implements Runnable {
    public static final String[] columnNames = {
            "MAC address",
            "Hostname", "Last timestamp"};

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (broadcasters) {
                    synchronized (pendingMessages) {//no modification allowed
                        broadcasters.forEach((k, v) -> ++v.skippedAnnounces);
                        pendingMessages.forEach((k, v) -> {
                            broadcasters.get(k).lastTimestamp = v.timestamp;
                            --broadcasters.get(k).skippedAnnounces;
                        });
                        pendingMessages.clear();
                        Set<String> toRemove = new HashSet<>();
                        broadcasters.keySet().stream()
                                .filter(key -> broadcasters.get(key).skippedAnnounces >= 5)
                                .forEach((s) -> {
                                    BroadcasterInfo info = broadcasters.get(s);
                                    System.out.format("Removed broadcaster with mac: %s, hostname = %s because of 5 missed announces\n",
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
        Object[][] data = new Object[broadcasters.values().size()][3];
        int k = 0;
        for (BroadcasterInfo info : broadcasters.values()) {
            data[k][0] = info.mac;
            data[k][1] = info.hostname;
            data[k][2] = info.lastTimestamp;
            ++k;
        }
        TextTable tt = new TextTable(columnNames, data);
        tt.setSort(0);
        tt.printTable();
    }
}
