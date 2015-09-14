package ru.ifmo.network;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public abstract class DatagramSender implements Runnable {
    protected final Logger logger = Logger.getLogger(DatagramSender.class);

    private final long sendDelay;

    private final InetSocketAddress broadcastAddress;

    public DatagramSender(int port, long sendDelay) throws SocketException {
        this.sendDelay = sendDelay;
        broadcastAddress = new InetSocketAddress(getBroadcastAddress(), port);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                send();
                Thread.sleep(sendDelay);
            }
        } catch (IOException e) {
            logger.error("Failed to create local Message", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void send() throws IOException ;

    public void sendBytes(byte[] bytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddress);
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.send(packet);
    }

    private static InetAddress getBroadcastAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast != null)
                    return broadcast;
            }
        }
        throw new SocketException("No broadcast address found");
    }

}
