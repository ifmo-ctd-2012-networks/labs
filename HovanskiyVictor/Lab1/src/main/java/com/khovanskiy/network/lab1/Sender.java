package com.khovanskiy.network.lab1;

import com.khovanskiy.network.lab1.model.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * @author victor
 */
public class Sender extends QueueRunnable<Sender.Request> {

    private OnSentListener listener;

    public Sender() {
    }

    public void send(InetSocketAddress address, Message message) {
        try {
            add(new Request(address, message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setOnSuccessListener(OnSentListener listener) {
        this.listener = listener;
    }

    @Override
    protected void handle(Request request) {
        byte[] bytes = request.message.getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, request.address);
            socket.send(packet);
            if (listener != null) {
                listener.onSuccess(request.message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface OnSentListener {
        void onSuccess(Message message);
    }

    protected class Request {
        private final InetSocketAddress address;
        private final Message message;

        public Request(InetSocketAddress address, Message message) {
            this.address = address;
            this.message = message;
        }
    }
}
