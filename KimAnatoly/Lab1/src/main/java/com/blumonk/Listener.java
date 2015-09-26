package com.blumonk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author akim
 */
public class Listener implements Runnable {

    private final int PORT;
    private final int PACKET_SIZE = 512;
    private final Map<Host, Integer> senders;
    private DatagramSocket socket;
    private Set<Host> gotPacketsFrom;

    public Listener(int PORT) {
        this.PORT = PORT;
        try {
            this.socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            System.err.println("Error while creating listener socket");
            e.printStackTrace();
            System.exit(1);
        }
        senders = new HashMap<>();
        gotPacketsFrom = new HashSet<>();
    }

    @Override
    public void run() {
        new Thread(new Printer()).start();
        while(true) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            try {
                socket.receive(datagramPacket);
                byte[] bytes = datagramPacket.getData();
                Packet packet = new Packet(Arrays.copyOfRange(bytes, 0, datagramPacket.getLength()));
                processPacket(packet);
            } catch (IOException | IllegalArgumentException e) {}
        }
    }

    private void processPacket(Packet packet) {
        synchronized (senders) {
            Host sender = new Host(packet);
            long timestamp = packet.getTimestamp();
            Date date = new Date(timestamp * 1000L);
            System.out.println("....... Received new packet from " + sender.toString()
                    + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            if (!senders.containsKey(sender)) {
                System.out.println("....... Added new active sender: " + sender.getHostname());
                senders.put(sender, 0);
            }
            gotPacketsFrom.add(sender);
        }
    }

    private void checkoutSenders() {
        synchronized (senders) {
            synchronized (gotPacketsFrom) {
                Iterator it = senders.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Integer> entry = (Map.Entry) it.next();
                    if (!gotPacketsFrom.contains(entry.getKey())) {
                        int missed = entry.getValue();
                        if (missed >= 5) {
                            it.remove();
                        } else {
                            entry.setValue(missed + 1);
                        }
                    }
                }
                gotPacketsFrom.clear();
            }
        }
    }

    class Printer implements Runnable {
        private final int PERIOD = 5000;
        private final String BORDER = "+-------------------------------------------------------------------+";
        private final String HEADER = "| %1$-35s| %2$-20s| %3$-7s|\n";
        private final String FORMAT = "| %1$-35s| %2$-20s| %3$-7d|\n";

        @Override
        public void run() {
            while (true) {
                try {
                    checkoutSenders();
                    showActiveSenders();
                    Thread.sleep(PERIOD);
                } catch (InterruptedException e) {}
            }
        }

        private void showActiveSenders() {
            synchronized (senders) {
                if (senders.isEmpty()) {
                    return;
                }
                System.out.println(BORDER);
                System.out.format(HEADER, "Hostname", "MAC", "Missed");
                System.out.println(BORDER);
                senders.forEach((host, missed) -> {
                    System.out.format(FORMAT, host.getHostname(),host.getMac(), missed);
                });
                System.out.println(BORDER);
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Broadcaster(1234, "Толик")).start();
        new Thread(new Listener(1234)).start();
    }
}
