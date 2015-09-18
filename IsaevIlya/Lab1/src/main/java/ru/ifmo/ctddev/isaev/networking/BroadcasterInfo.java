package ru.ifmo.ctddev.isaev.networking;

/**
 * @author Ilya Isaev
 */
public class BroadcasterInfo {
    String mac;
    String hostname;
    int skippedAnnounces = 0;

    public BroadcasterInfo(Message msg) {
        this.mac = msg.mac;
        this.hostname = msg.hostname;
    }
}
