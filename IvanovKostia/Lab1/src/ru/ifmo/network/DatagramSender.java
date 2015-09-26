package ru.ifmo.network;


import org.apache.log4j.Logger;
import ru.ifmo.threads.ClosableRunnable;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class DatagramSender implements ClosableRunnable {
    protected final Logger logger = Logger.getLogger(DatagramSender.class);

    private final long sendDelay;

    private final List<InetAddress> broadcastAddresses;
    private final int port;

    private final DatagramSocket socket = new DatagramSocket();

    private boolean closed;

    public DatagramSender(int port, long sendDelay, NetworkInterface networkInterface) throws SocketException {
        this.sendDelay = sendDelay;
        this.port = port;
        socket.setBroadcast(true);

        broadcastAddresses = getBroadcastAddress(networkInterface);
        if (broadcastAddresses.size() == 0) {
            throw new SocketException("No broadcast addresses found");
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                send();
                Thread.sleep(sendDelay);
            }
        } catch (IOException e) {
            if (!closed) {
                logger.error("Failed to send", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Sender shutdown due to exception", e);
        } finally {
            close();
        }
    }

    protected abstract void send() throws IOException;

    protected void sendBytes(byte[] bytes) throws IOException {
        for (InetAddress broadcastAddress : broadcastAddresses) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddress, port);
            socket.send(packet);
        }
    }

    public void close() {
        closed = true;
        socket.close();
    }

    private static List<InetAddress> getBroadcastAddress(NetworkInterface networkInterface) throws SocketException {
        List<InetAddress> result = networkInterface.getInterfaceAddresses()
                .stream()
                .map(InterfaceAddress::getBroadcast)
                .filter(Objects::nonNull)
                .limit(0)
                .collect(Collectors.toList());
        try {
            result.add(InetAddress.getByName("255.255.255.255"));
        } catch (UnknownHostException e) {
            throw new Error("Unexpected exception", e);
        }

        // TODO: how to choose a single address?
//        return result;
        return result.stream()
                .limit(1)
                .collect(Collectors.toList());
    }

}
