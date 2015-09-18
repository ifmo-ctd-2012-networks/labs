package ru.ifmo.ctdev.koshik.networks.lab1;

import java.net.*;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Main implements Runnable{

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    private List<Instance> instances;

    private Receiver receiver;

    private static final int PERIOD = 5000;

    private static final int MAX_MISSED_PACKETS = 5;


    public Main(Receiver receiver) {
        instances = new ArrayList<>();
        this.receiver = receiver;
        LOGGER.addHandler(new ConsoleHandler());
        LOGGER.setUseParentHandlers(false);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Specify port.");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try {
            NetworkInterface ni = getNetworkInterface();
            if (ni == null) {
                System.err.println("Couldn't find any appropriate interface.");
                return;
            }
            Thread sender = new Thread(new Sender(port, PERIOD, ni));
            Receiver receiver = new Receiver(port);
            Thread receiverThread = new Thread(receiver);

            Thread main = new Thread(new Main(receiver));

            sender.start();
            receiverThread.start();
            main.start();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static NetworkInterface getNetworkInterface() throws SocketException {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            if (!ni.isLoopback() && ni.isUp() && ni.getHardwareAddress() != null) {
                InetAddress broadcast = null;
                for (InterfaceAddress address : ni.getInterfaceAddresses()) {
                    broadcast = address.getBroadcast();
                    if (broadcast != null)
                        break;
                }
                if (broadcast != null)
                    return ni;
            }
        }
        return null;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<Packet> packets = receiver.getPackets();
            for (Packet packet : packets) {
                handlePackage(packet);
            }

            List<Integer> instancesToRemove = new ArrayList<>();
            int k = 0;
            for (Instance i : instances) {
                if (!i.receivedPacket()) {
                    if (i.incMissedPacketsCount() >= MAX_MISSED_PACKETS)
                        instancesToRemove.add(k);
                }
                k++;
                i.resetReceivedPacket();
            }

            for (int l : instancesToRemove) {
                Instance removed = instances.remove(l);
                LOGGER.info("Instance (MAC address = " + removed.macAddress + ", hostname = " + removed.hostname + ")" +
                            " was removed\n");
            }

            try {
                Thread.sleep(PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void handlePackage(Packet packet) {
        Instance instance = new Instance(packet.macAddress, packet.hostname);

        int index = Collections.binarySearch(instances, instance);
        if (index < 0) {
            instance.setReceivedPacket();
            int insertIndex = - (index) - 1;
            instances.add(insertIndex, instance);
        } else {
            instances.get(index).setReceivedPacket();
        }
    }
}
