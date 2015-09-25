package com.lukin.network.lab1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by Саша on 18.09.2015.
 */
public class Server extends LinkedBlockingQueueRunnable<Server.Request> {
    private MessageListener messageListener;

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void send(InetSocketAddress inetSocketAddress, Data data){
        try {
            put(new Request(inetSocketAddress, data));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute(Request request) {
        byte[] bytes = request.data.getBytes();
        try(DatagramSocket socket = new DatagramSocket()){
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, request.address);
            socket.send(packet);
            if (messageListener != null){
                messageListener.onSuccess(request.data);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    protected class Request {
        private final InetSocketAddress address;
        private final Data data;

        public Request(InetSocketAddress address, Data data) {
            this.address = address;
            this.data = data;
        }
    }
    public interface MessageListener {
        void onSuccess(Data data);
    }
}
