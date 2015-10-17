package ru.ifmo.ctddev.efimova.net.udp;

import static ru.ifmo.ctddev.efimova.net.udp.Constants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable {

    private BlockingQueue<Message> incomingMessages;

    public Server() {
        incomingMessages = new ArrayBlockingQueue<Message>(QUE_SIZE);
    }

    public void init() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new ServerTask(incomingMessages), 0, PERIOD);
    }

    @Override
    public void run() {
        try {
            init();
            DatagramSocket serverSocket = new DatagramSocket(PORT);
            byte[] name = new byte[MSG_SIZE];
            while (true) {
                byte[] receiveData = new byte[MSG_SIZE];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // read MAC-address
                byte[] macBytes = Arrays.copyOfRange(receiveData, 0, MAC_LEN);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < MAC_LEN; i++) {
                    sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? "-" : ""));
                }
                String mac = sb.toString();

                int hostname_len = receiveData[MAC_LEN];

                // read hostname
                for (int i = MAC_LEN + 1, name_ind = 0; name_ind < hostname_len; i++, name_ind++) {
                    name[name_ind] = receiveData[i];
                }
                String n = "";
                if (hostname_len > 0) {
                    n = new String(Arrays.copyOfRange(name, 0, hostname_len));
                }

                // read timestamp
                int ts = 0;
                if (hostname_len > 0) {
                    int ind = MAC_LEN + 1 + hostname_len;
                    ByteBuffer bbuf = ByteBuffer.wrap(receiveData, ind, 4);
                    ts = bbuf.getInt();
                }

                Message msg = new Message(mac, n, ts);
                incomingMessages.offer(msg);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
