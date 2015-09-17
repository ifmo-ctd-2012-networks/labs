package ru.ifmo.ctddev.isaev.networking;

import java.util.Arrays;

import static ru.ifmo.ctddev.isaev.networking.Main.broadcasters;
import static ru.ifmo.ctddev.isaev.networking.Main.pendingMessages;

/**
 * @author Ilya Isaev
 */
public class Printer implements Runnable {


  /*  private void printInfo() {
        System.out.println("Broadcasters: ");
        for (Message message : broadcasters) {
            System.out.println(String.format("mac: %s, hostname = %s", Arrays.toString(message.mac.getBytes()), message.hostname));
        }
        System.out.println("_____________________");
    }*/

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (broadcasters) {
                    synchronized (pendingMessages) {//no modification allowed
                        broadcasters.forEach((k, v) -> ++v.skippedAnnounces);
                        pendingMessages.forEach((k, v) -> --broadcasters.get(k).skippedAnnounces);
                        pendingMessages.clear();
                        broadcasters.keySet().stream()
                                .filter(key -> broadcasters.get(key).skippedAnnounces >= 5)
                                .forEach((s) -> {
                                    BroadcasterInfo info = broadcasters.get(s);
                                    System.out.println(String.format("Removed broadcaster with mac: %s, hostname = %s",
                                            Arrays.toString(info.mac.getBytes()), info.hostname));
                                    broadcasters.remove(s);
                                });
                        pendingMessages.clear();
                    }
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
