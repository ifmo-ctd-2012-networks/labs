package ru.aleynikov.net.lab1;

public class Main {

    public static void main(String[] args) {
        try {
            int portNum = Integer.parseInt(args[0]);
            new Thread(new Client(portNum)).start();
            new Thread(new Server(portNum)).start();
            new Thread(new Output()).start();
        } catch (NumberFormatException e) {
            System.err.println("Port number must be in the first argument");
        }
    }
}
