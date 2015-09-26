import exceptions.ProtoException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Client implements Runnable {

    private static final int PACKET_MAX_LENGTH = 500;

    private final Map<String, Packet> instances = new HashMap<>();
    private final Map<String, Integer> missedPackets = new HashMap<>();

    private final int port;

    public Client(int port) {
        this.port = port;
        new PrintThread().start();
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setBroadcast(true);
            while (!Thread.currentThread().isInterrupted()) {
                byte[] packetBuffer = new byte[PACKET_MAX_LENGTH];
                DatagramPacket datagramPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
                try {
                    socket.receive(datagramPacket);
                    byte[] buffer = datagramPacket.getData();
                    Packet packet = Packet.newInstance(buffer);
                    received(packet);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (ProtoException e) {
                    System.out.println("Invalid packet data received");
                }
            }

        } catch (SocketException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    private class PrintThread extends Thread {

        private final static int MAX_LOST_COUNT = 5;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(Server.SLEEP_TIMEOUT);
                    System.out.println("########################");
                    printInstances();
                    System.out.println("########################");
                    System.out.println();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void printInstances() {
            Set<String> toDelete = new HashSet<>();
            for (String address : instances.keySet()) {
                if (missedPackets.get(address) >= MAX_LOST_COUNT) {
                    toDelete.add(address);
                } else {
                    missedPackets.put(address, missedPackets.get(address) + 1);
                    Packet packet = instances.get(address);
                    System.out.println(
                            "macAddress = " + address +
                            ", hostName = " + packet.getName() +
                            ", timestamp = " + packet.getTimestamp() +
                            ", missed count = " + missedPackets.get(address));
                }
            }

            for (String delete : toDelete) {
                instances.remove(delete);
                missedPackets.remove(delete);
            }
        }
    }

    private void received(Packet packet) {
        String address = packet.getMacAddress();
        missedPackets.put(address, 0);
        instances.put(address, packet);
    }
}
