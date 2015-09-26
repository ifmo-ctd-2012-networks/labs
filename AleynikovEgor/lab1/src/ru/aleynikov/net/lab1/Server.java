package ru.aleynikov.net.lab1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Server implements Runnable {
    private static int BUFFER_SIZE = 1024;
    private int portNum;

    public Server(int portNum) {
        this.portNum = portNum;
    }

    @Override
    public void run() {
        try (DatagramSocket ds = new DatagramSocket(portNum)) {
            while(true){
                DatagramPacket dp = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                try {
                    ds.receive(dp);
                    try {
                        Info.Message info = new Info.Message(Arrays.copyOfRange(dp.getData(), 0, dp.getLength()));
                        Info.INSTANCE.updateInfo(info);
                    } catch (Info.BadInfoException e) {
                        System.err.println("Wrong DatagramPacket from " + dp.getAddress().getHostAddress() + ": " + e.getMessage());
                    }
                } catch (IOException e) {}
            }
        } catch (SocketException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}
