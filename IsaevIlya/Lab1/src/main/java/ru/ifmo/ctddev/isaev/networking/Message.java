package ru.ifmo.ctddev.isaev.networking;

import java.util.Arrays;

/**
 * @author Ilya Isaev
 */
public class Message {
    String mac;
    String hostname;
    long timestamp;

    @Override
    public String toString() {
        return "Message{" +
                "mac='" + Arrays.toString(mac.getBytes()) + '\'' +
                ", hostname='" + hostname + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
