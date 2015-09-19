package ru.ifmo.ctddev.isaev.networking;

/**
 * @author Ilya Isaev
 */
public class Message {
    Long mac;
    String hostname;
    long timestamp;

    @Override
    public String toString() {
        return "Message{" +
                "mac='" + mac + '\'' +
                ", hostname='" + hostname + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
