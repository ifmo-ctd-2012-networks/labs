package com.lukin.network.lab1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by Саша on 18.09.2015.
 */
public class Client implements Runnable {
    private final int port;
    private static final int MESSAGE_LENGTH = 1551; // 6 + 1 + 6 * 256 + 8
    private MessageListener messageListener;

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            while (!Thread.interrupted()){
                DatagramPacket packet = new DatagramPacket(new byte[MESSAGE_LENGTH], MESSAGE_LENGTH);
                socket.receive(packet);
                Data data = Data.convertBytes(packet.getData());
                if (messageListener != null){
                    messageListener.onSuccess(data);
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }
    public interface MessageListener {
        void onSuccess(Data data);
    }
}
