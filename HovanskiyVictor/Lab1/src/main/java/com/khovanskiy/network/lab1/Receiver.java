package com.khovanskiy.network.lab1;

import com.khovanskiy.network.lab1.model.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author victor
 */
public class Receiver implements Runnable {

    private final static int PACKET_LENGTH = 6 * Byte.BYTES + Byte.BYTES + 6 * 256 * Byte.BYTES + Long.BYTES;
    private final int port;
    private OnReceivedListener listener;

    public Receiver(int port) {
        this.port = port;
    }

    public void setOnSuccessListener(OnReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            while (!Thread.interrupted()) {
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
    public interface OnReceivedListener {
        void onSuccess(Message message);
    }
}
