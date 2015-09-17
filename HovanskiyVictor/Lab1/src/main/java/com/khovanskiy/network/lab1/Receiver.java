package com.khovanskiy.network.lab1;

import com.khovanskiy.network.lab1.model.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author victor
 */
public class Receiver implements Runnable {

    private OnRecievedListener listener;
    private final int port;

    public Receiver(int port) {
        this.port = port;
    }

    private int PACKET_LENGTH = 512;

    public void setOnSuccessListener(OnRecievedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[PACKET_LENGTH], PACKET_LENGTH);
                socket.receive(packet);
                Message message = new Message(packet.getData());
                if (listener != null) {
                    listener.onSuccess(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface OnRecievedListener {
        void onSuccess(Message message);
    }

    protected class Response {

    }
}
