package ru.ifmo.ctddev.isaev.networking;

import java.util.Arrays;

import static ru.ifmo.ctddev.isaev.networking.Main.broadcasters;

/**
 * @author Ilya Isaev
 */
public class Printer implements Runnable {


    private void printInfo() {
        System.out.println("Broadcasters: ");
        for (Message message : broadcasters) {
            System.out.println(String.format("mac: %s, hostname = %s", Arrays.toString(message.mac.getBytes()), message.hostname));
        }
        System.out.println("_____________________");
    }

    @Override
    public void run() {
        while (true) {
            printInfo();
            Thread.sleep(5000);
        }
    }
}
