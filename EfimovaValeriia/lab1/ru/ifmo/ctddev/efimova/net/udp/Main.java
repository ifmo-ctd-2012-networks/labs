package ru.ifmo.ctddev.efimova.net.udp;

public class Main {

    public static void main(String[] args) {
        Thread clientThread = new Thread(new Client());
        clientThread.start();
        Thread serverThread = new Thread(new Server());
        serverThread.start();
    }
}
