package ru.ifmo.ctddev.efimova.net.udp;

public class Message {

    String mac, hostname;
    int timestamp;

    public Message(String mac, String hostname, int timestamp) {
        this.mac = mac;
        this.hostname = hostname;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MAC = " + mac + ", hostname = " + hostname + ", timestamp = " + timestamp;
    }
}
