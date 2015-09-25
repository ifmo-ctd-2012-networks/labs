package ru.ifmo.isomurodov.broadcast;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by javlon on 18.09.15.
 */
public class Sender implements Runnable {
    private int port;

    public Sender(int port){
        this.port = port;
    }


    @Override
    public void run() {
        try {
            String hostname = null;
            hostname = InetAddress.getLocalHost().getHostName();
            if(hostname == null){
                throw new IllegalArgumentException("");
            }
            DatagramSocket socket = null;
            socket = new DatagramSocket();
            while (true) {
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface network = interfaces.nextElement();
                        List<InterfaceAddress> addresses = network.getInterfaceAddresses();
                        for (InterfaceAddress ia : addresses) {
                            if (ia.getBroadcast() != null) {
                                byte[] name = hostname.getBytes(Charset.forName("UTF-8"));
                                long timeStamp = System.currentTimeMillis();
                                timeStamp /= 1000;
                                ByteBuffer buf = ByteBuffer.allocate(4);
                                buf.order(ByteOrder.BIG_ENDIAN);
                                buf.putInt((int) timeStamp);
                                UDP info = new UDP(network.getHardwareAddress(), name, buf.array());
                                byte[] data = info.getBytes();
                                DatagramPacket packet = new DatagramPacket(data, data.length, ia.getBroadcast(), port);
                                socket.send(packet);
                            }
                        }
                    }
                    Thread.sleep(Main.TIME);
                } catch (IOException ignored) {} catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
