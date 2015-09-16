package ru.ifmo.ctddev.isaev.networking;

/**
 * @author Ilya Isaev
 */
public class BroadcasterInfo {
    String mac;
    int skippedAnnounce = 0;

    public BroadcasterInfo(String mac) {
        this.mac = mac;
    }
}
