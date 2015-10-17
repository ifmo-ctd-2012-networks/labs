package ru.ifmo.network;


import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DatagramSender implements AutoCloseable {
    private final Logger logger = Logger.getLogger(DatagramSender.class);

    private final List<InetAddress> broadcastAddresses;
    private final int port;

    private final DatagramSocket socket = new DatagramSocket();

    public DatagramSender(int port) throws SocketException {
        this.port = port;
        socket.setBroadcast(true);

        broadcastAddresses = getBroadcastAddress();
        if (broadcastAddresses.size() == 0) {
            throw new SocketException("No broadcast addresses found");
        }
    }

    protected void sendBytes(byte[] bytes) {
        for (InetAddress broadcastAddress : broadcastAddresses) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddress, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                logger.warn("Writing to socket failed");
                socket.close();
            }
        }
    }

    public void close() {
        socket.close();
    }

    private static List<InetAddress> getBroadcastAddress() throws SocketException {
        List<InetAddress> result = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .map(NetworkInterface::getInterfaceAddresses)
                .map(List::stream)
                .map(stream -> stream.limit(1))
                .flatMap(Function.identity())
                .map(InterfaceAddress::getBroadcast)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        try {
            result.add(InetAddress.getByName("255.255.255.255"));
        } catch (UnknownHostException e) {
            throw new Error("Unexpected exception", e);
        }

        return result;
    }

}
