package ru.ifmo.ctddev.varlamov;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Client implements Runnable {

    private static final int PERIOD = 5000;
    private static final int MAX_PACKET_SIZE = 1024;
    private static final int MAX_MISSED = 5;

    private final Map<String, Integer> missedPackets = new ConcurrentHashMap<>();
    private final Map<String, String> macAddresses = new ConcurrentHashMap<>();
    private final Set<String> announcedHosts = new ConcurrentSkipListSet<>();
    private final Logger logger;
    private final int port;

    public Client(Logger logger, int port) {
        this.logger = logger;
        this.port = port;
    }

    private class PacketInfo {
        private String macAddress;
        private String host;
        private long unixTime;
        private Date unixTimeDate;

        public PacketInfo(DatagramPacket datagramPacket) throws Exception {
            byte[] data = datagramPacket.getData();
            StringBuilder sb = new StringBuilder(18);
            for (byte b : Arrays.copyOfRange(data, 0, 6)) {
                if (sb.length() > 0)
                    sb.append(':');
                sb.append(String.format("%02x", b));
            }
            macAddress = sb.toString();
            host = new String(Arrays.copyOfRange(data, 7, 7 + data[6]), Charset.forName("UTF-8"));
            /*byte[] ut = Arrays.copyOfRange(data, 7 + data[6], 7 + data[6] + 8);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.put(ut);
            buffer.flip();
            unixTime = buffer.getLong();/**/
            byte[] ut = Arrays.copyOfRange(data, 7 + data[6], 7 + data[6] + 4);
            long ff = 0xFF;
            unixTime = ((ut[0] & ff) << 24) + ((ut[1] & ff) << 16) + ((ut[2] & ff) << 8) + (ut[3] & ff);
            unixTimeDate = new Date();
            unixTimeDate.setTime(unixTime * 1000);
        }
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Set<String> hosts;
                hosts = missedPackets.keySet();
                logger.log("\nHost info:");
                ArrayList<String> sortedHosts = new ArrayList(hosts);
                Collections.sort(sortedHosts, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return macAddresses.get(o1).compareTo(macAddresses.get(o2));
                    }
                });
                for (String s : sortedHosts) {
                    int missed = missedPackets.get(s);
                    if (!announcedHosts.contains(s)) {
                        missed++;
                    } else {
                        missed = 0;
                    }
                    if (missed == MAX_MISSED) {
                        logger.log("Host " + s + " removed for " + MAX_MISSED + " misses");
                        missedPackets.remove(s);
                        macAddresses.remove(s);
                    } else {
                        logger.log("Host " + s + " with MAC address " + macAddresses
                                .get(s) + " missed " + missed + " announces");
                        missedPackets.put(s, missed);
                    }
                }
                //logger.log("\n");
                announcedHosts.clear();
            }
        }, 0, PERIOD);
        while (true) {
            try {
                DatagramSocket socket = new DatagramSocket(port);

                DatagramPacket packet;
                while (true) {
                    try {
                        byte[] buf = new byte[MAX_PACKET_SIZE];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        PacketInfo packetInfo = new PacketInfo(packet);
                        announcedHosts.add(packetInfo.host);
                        if (!missedPackets.containsKey(packetInfo.host)) {
                            missedPackets.put(packetInfo.host, 0);
                        }
                        macAddresses.put(packetInfo.host, packetInfo.macAddress);
                        //logger.log("Received packet:");
                        //logger.log("MAC address = " + packetInfo.macAddress);
                        //logger.log("Host = " + packetInfo.host);
                        //logger.log("Unix time = " + packetInfo.unixTimeDate);
                    } catch (Exception e) {
                        logger.log(e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}