package ru.georgeee.itmo.sem7.networks.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

@Component
public class Sender implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(Sender.class);

    @Autowired
    private Settings settings;


    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            while (true) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }


                Message msg = createMessage();
                log.debug("Sending message: {}", msg);
                byte[] msgBytes = msg.getBytes();
                for(InterfaceAddress address : settings.getNetworkInterface().getInterfaceAddresses()){
                    if(address.getBroadcast() != null) {
                        datagramSocket.send(new DatagramPacket(msgBytes, 0, msgBytes.length, address.getBroadcast(), settings.getPort()));
                    }
                }

                try {
                    Thread.sleep(settings.getInterval() * 1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (IOException e) {
            log.info("Caught exception", e);
        }
    }

    private Message createMessage() throws SocketException {
        return new Message(settings.getNetworkInterface().getHardwareAddress(), settings.getHostName());
    }

}
