package ru.ifmo.ctddev.network;

public class Message {
    private String mac;
    private int hostname_length;
    private String hostname;
    private Long timestamp;

    public Message(String mac, int hostname_length, String hostname, Long timestamp) {
        this.mac = mac;
        this.hostname = hostname;
        this.hostname_length = hostname_length;
        this.timestamp = timestamp;
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

    public Long getTimestamp() {
        return timestamp;
    }

}
