package ru.ifmo.ctddev.isaev.networking;

/**
 * @author Ilya Isaev
 */
public class BroadcasterInfo {
    String mac;
    String hostname;
    long lastTimestamp;
    int skippedAnnounces = 0;

    public BroadcasterInfo(Message msg) {
        this.mac = msg.mac;
        this.hostname = msg.hostname;
        this.lastTimestamp = msg.timestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
}
