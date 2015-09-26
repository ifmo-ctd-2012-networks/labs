package ru.ifmo.ctddev.network;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

public class HostPerson {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String mac;
    private String hostname;
    private int hostname_length;
    private long timestamp;
    private long lastReceived;
    private Queue<Long> history;

    public HostPerson(String mac, int hostname_length, String hostname, long timestamp, long lastReceived) {
        this.mac = mac;
        this.hostname_length = hostname_length;
        this.hostname = hostname;
        this.timestamp = timestamp;
        this.lastReceived = lastReceived;
        this.history = new ArrayDeque<Long>(20);
    }

    public String getMac() {
        return mac;
    }

    public String getHostname() {
        return hostname;
    }

    public int getHostnameLength() {
        return hostname_length;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastReceived() {
        return lastReceived;
    }

    public int getPacketsLost() {
        return history.size();
    }

    public void updateHistory(long current, boolean received) {
        if (current - lastReceived > 5000) {
            history.add(current);
        }

        if (received) {
            lastReceived = current;
        }

        while (!history.isEmpty() && current - history.peek() > 5000) {
            history.poll();
        }
    }

    private static String fitToWidth(String s, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.substring(0, Math.min(width, s.length())));
        width -= s.length();
        for (; width > 0; width--) {
            sb.append(' ');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "| MAC: " + fitToWidth(getMac(), 18) +
                " | HOSTNAME: " + fitToWidth(getHostname(), 17) +
                " | HOSTNAME_LENGTH: " + fitToWidth(String.valueOf(getHostnameLength()), 5) +
                " | TIMESTAMP: " + fitToWidth(String.valueOf(timestamp), 10) +
                " | Lost: " + fitToWidth(String.valueOf(getPacketsLost()), 2) +
                " | Last received: " + dateFormat.format(new Date(getLastReceived()));
    }
}
