package ru.ifmo.loboda.net;

public class Main {
    public static void main(String[] args) throws BadPacketException {
        try {
            int port = Integer.parseInt(args[0]);
            Thread broadcaster = new Thread(new Broadcaster(port));
            DB db = new DB();
            Thread listener = new Thread(new Listener(port, db));
            Thread ioThread = new Thread(new IOThread(db));
            broadcaster.start();
            listener.start();
            ioThread.start();
        } catch (NumberFormatException e) {
            System.err.println("Need port number as arg");
        }
    }
}
