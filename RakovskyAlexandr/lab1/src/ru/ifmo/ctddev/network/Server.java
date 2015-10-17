package ru.ifmo.ctddev.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable {

    public static final int MAC_LENGTH = 6;
    public static final int HOSTNAME_L = 1;
    public static final int TIMESTAMP_LENGTH = 8;

    private BlockingQueue<Message> incomingMessages;

    public Server() {
        incomingMessages = new ArrayBlockingQueue<>(20);
    }

    @Override
    public void run() {
        try {
            Thread msgWorker = new Thread(new MessageWorker(incomingMessages));
            msgWorker.start();

            DatagramSocket socket = new DatagramSocket(6969);

            while (true) {
                byte[] receivedData = new byte[1024];

                DatagramPacket received = new DatagramPacket(receivedData, receivedData.length);
                socket.receive(received);

                byte[] macBytes = Arrays.copyOfRange(receivedData, 0, MAC_LENGTH);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < macBytes.length; i++) {
                    sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? "-" : ""));
                }
                String mac = sb.toString();

                byte[] hostname_length_bytes = Arrays.copyOfRange(receivedData, MAC_LENGTH, MAC_LENGTH + HOSTNAME_L);
                int hostname_length = (int)hostname_length_bytes[0];

                byte[] hostname_bytes = Arrays.copyOfRange(receivedData, MAC_LENGTH + HOSTNAME_L, MAC_LENGTH + HOSTNAME_L + hostname_length);
                String hostname = new String(hostname_bytes);

                byte[] timestamp_bytes = Arrays.copyOfRange(receivedData, MAC_LENGTH + HOSTNAME_L + hostname_length, MAC_LENGTH + HOSTNAME_L + hostname_length + TIMESTAMP_LENGTH);
                Long timestamp = ByteBuffer.wrap(timestamp_bytes).order(ByteOrder.BIG_ENDIAN).getLong();

                Message msg = new Message(mac, hostname_length, hostname, timestamp);
                incomingMessages.offer(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
