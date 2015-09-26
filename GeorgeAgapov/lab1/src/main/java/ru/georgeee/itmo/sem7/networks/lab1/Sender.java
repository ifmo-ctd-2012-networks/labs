package ru.georgeee.itmo.sem7.networks.lab1;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InterfaceAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

@Component
public class Sender implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(Sender.class);
    private final Random random = new Random(System.currentTimeMillis());
    @Autowired
    private Settings settings;
    @Value("${strategy:NORMAL}")
    private String sendingStrategyString;
    private Sender.SendingStrategy sendingStrategy;
    @Autowired
    private ReceivedMap receivedMap;

    public static String[] getStrategyNames() {
        return Arrays.asList(SendingStrategy.values()).stream().map(SendingStrategy::name).toArray(String[]::new);
    }

    @PostConstruct
    public void init() {
        try {
            sendingStrategy = SendingStrategy.valueOf(sendingStrategyString.toUpperCase());
        } catch (IllegalArgumentException e) {
            sendingStrategy = SendingStrategy.NORMAL;
        }
        log.info("Using sending strategy: {}", sendingStrategy);
    }

    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            while (true) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }

                sendingStrategy.function.apply(this, datagramSocket);
            }
        } catch (SendException e) {
            if (e.getCause() instanceof IOException) {
                log.info("Caught exception", e.getCause());
            } else {
                throw e;
            }
        } catch (IOException e) {
            log.info("Caught exception", e);
        }
    }

    private Void normalSend(DatagramSocket datagramSocket) {
        try {
            Message msg = new Message(settings.getNetworkInterface().getHardwareAddress(), settings.getHostName());
            sendMessage(msg, datagramSocket);

            try {
                Thread.sleep(settings.getInterval() * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            throw new SendException(e);
        }
        return null;
    }

    private Void attack1Send(DatagramSocket datagramSocket) {
        try {
            sendMessage(new BreakingMessage(), datagramSocket);
            sleep();
        } catch (IOException e) {
            throw new SendException(e);
        }
        return null;
    }

    private Void attack2Send(DatagramSocket datagramSocket) {
        try {
            for (Pair<Long, Message> pair : receivedMap.values()) {
                long macAddress = pair.getRight().getMacAddress();
                String hostName = pair.getRight().getHostName();
                sendMessage(new Message(macAddress, hostName), datagramSocket);
            }
            sendSelfMessage(datagramSocket);
            sleep();
        } catch (IOException e) {
            throw new SendException(e);
        }
        return null;
    }

    private Void attack3Send(DatagramSocket datagramSocket) {
        try {
            sendSelfMessage(datagramSocket);
        } catch (IOException e) {
            throw new SendException(e);
        }
        return null;
    }

    private Void attack4Send(DatagramSocket datagramSocket) {
        try {
            sendMessage(new Message(random.nextLong(), settings.getHostName()), datagramSocket);
        } catch (IOException e) {
            throw new SendException(e);
        }
        return null;
    }

    private void sendSelfMessage(DatagramSocket datagramSocket) throws IOException {
        Message msg = new Message(settings.getNetworkInterface().getHardwareAddress(), settings.getHostName());
        sendMessage(msg, datagramSocket);
    }

    private void sleep() {
        try {
            Thread.sleep(settings.getInterval() * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendMessage(Message msg, DatagramSocket datagramSocket) throws IOException {
        log.debug("Sending message: {}", msg);
        byte[] msgBytes = msg.getBytes();
        for (InterfaceAddress address : settings.getNetworkInterface().getInterfaceAddresses()) {
            if (address.getBroadcast() != null) {
                datagramSocket.send(new DatagramPacket(msgBytes, 0, msgBytes.length, address.getBroadcast(), settings.getPort()));
            }
        }
    }

    private enum SendingStrategy {
        NORMAL(Sender::normalSend), ATTACK3(Sender::attack3Send), ATTACK4(Sender::attack4Send), ATTACK1(Sender::attack1Send), ATTACK2(Sender::attack2Send);

        private final BiFunction<Sender, DatagramSocket, ?> function;

        SendingStrategy(BiFunction<Sender, DatagramSocket, ?> function) {
            this.function = function;
        }
    }

    private static class SendException extends RuntimeException {
        public SendException(Throwable cause) {
            super("Exception caught in sender", cause);
        }
    }

}
