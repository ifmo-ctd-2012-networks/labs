package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;

class TRListener<D extends Data<D>> extends Thread implements TRHandler {
    private final static Logger log = LoggerFactory.getLogger(TRListener.class);

    private final Context<D> context;
    private final DatagramSocket socket;

    public TRListener(Context<D> context, DatagramSocket socket) {
        this.context = context;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }

                byte[] buf = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buf, 4096);
                socket.receive(packet);
                context.getExecutor().submit(() -> processPacket(packet));
            }
        } catch (IOException e) {
            log.info("Caught exception", e);
        }
    }

    private void processPacket(DatagramPacket packet) {
        try {
            context.getMessageService().handleTRMessage(packet, this);
        } catch (ParseException e) {
            log.warn("Error trying to parse message");
            log.debug("Error trying to parse message {}", packet.getData());
        }
    }

    @Override
    public void handleTR1(InetAddress address, int port, int tokenId) {

    }

    @Override
    public void handleTR2(InetAddress address, int port, int tokenId) {

    }

    private void sendMessage(byte[] msgBytes, DatagramSocket datagramSocket) throws IOException {
        for (InterfaceAddress address : context.getSettings().getNetworkInterface().getInterfaceAddresses()) {
            if (address.getBroadcast() != null) {
                datagramSocket.send(new DatagramPacket(msgBytes, 0, msgBytes.length, address.getBroadcast(), context.getSettings().getUdpPort()));
            }
        }
    }

}

