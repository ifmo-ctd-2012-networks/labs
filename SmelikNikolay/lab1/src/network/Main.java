package network;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Thread serverThread = new Thread(new UDPServer(6969));
            serverThread.start();
            Thread clientThread = new Thread(new UDPClient(6969));
            clientThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
