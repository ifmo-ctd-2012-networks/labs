package ru.ifmo.ctddev.varlamov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int MAX_MISSED = 5;
    private static final int PERIOD = 5000;
    private static final int SERVER_PORT = 4440;
    private static final String HOSTNAME = "sample host";
    private static final int MULTICAST_PORT = 4445;
    private static final String MULTICAST_ADDRESS = "224.0.113.0";

    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    private static final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    private static class Server implements Runnable {

        private class SocketHolder {
            private DatagramSocket socket;
            private byte[] hardwareAddress;

            private SocketHolder(NetworkInterface ni) throws SocketException {
                hardwareAddress = ni.getHardwareAddress();
                socket = new DatagramSocket(SERVER_PORT, ni.getInetAddresses().nextElement());
            }

            private DatagramPacket getPacket() {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                os.write(hardwareAddress, 0, 6);
                byte[] hostname = Charset.forName("UTF-8").encode(HOSTNAME).array();
                byte[] length = new byte[]{(byte) hostname.length};
                os.write(length, 0, 1);
                os.write(hostname, 0, hostname.length);
                long unixTime = (System.currentTimeMillis() / 1000L);
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(unixTime);
                byte[] unixBytes = buffer.array();
                os.write(unixBytes, 0, Long.BYTES);
                byte[] bytes = os.toByteArray();
                return new DatagramPacket(bytes, bytes.length, group, MULTICAST_PORT);
            }
        }

        private List<SocketHolder> socketHolders = new ArrayList<>();
        private InetAddress group;

        public Server() {
            try {
                group = InetAddress.getByName(MULTICAST_ADDRESS);
                for (Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces(); eni.hasMoreElements(); ) {
                    NetworkInterface ni = eni.nextElement();
                    if (!ni.isLoopback() && ni.isUp() && ni.supportsMulticast()) {
                        socketHolders.add(new SocketHolder(ni));
                    }
                }
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (SocketHolder socketHolder : socketHolders) {
                        try {
                            socketHolder.socket.send(socketHolder.getPacket());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, PERIOD);
        }
    }

    private static class Logger implements Runnable {

        private PrintStream out;

        public Logger(PrintStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queue) {
                    try {
                        queue.wait();
                        while (!queue.isEmpty()) {
                            out.println(queue.poll());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class Client implements Runnable {

        private final Map<String, Integer> missedPackets = new HashMap<>();
        private final Map<String, String> macAddresses = new HashMap<>();
        private final Set<String> announcedHosts = new HashSet<>();

        private class PacketInfo {
            private String macAddress;
            private String host;
            private long unixTime;
            private Date unixTimeDate;

            public PacketInfo(DatagramPacket datagramPacket) {
                byte[] data = datagramPacket.getData();
                StringBuilder sb = new StringBuilder(18);
                for (byte b : Arrays.copyOfRange(data, 0, 6)) {
                    if (sb.length() > 0)
                        sb.append(':');
                    sb.append(String.format("%02x", b));
                }
                macAddress = sb.toString();
                host = new String(Arrays.copyOfRange(data, 7, 7 + data[6]), Charset.forName("UTF-8"));
                byte[] ut = Arrays.copyOfRange(data, 7 + data[6], 7 + data[6] + Long.BYTES);
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.put(ut);
                buffer.flip();
                unixTime = buffer.getLong();
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
                    synchronized (missedPackets) {
                        hosts = missedPackets.keySet();
                        synchronized (announcedHosts) {
                            synchronized (queue) {
                                queue.add("\nHost info:");
                                synchronized (macAddresses) {
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
                                            queue.add("Host " + s + " removed for " + MAX_MISSED + " misses");
                                            missedPackets.remove(s);
                                            macAddresses.remove(s);
                                        } else {
                                            queue.add("Host " + s + " with MAC address " + macAddresses.get(s) + " missed " + missed + " announces");
                                            missedPackets.put(s, missed);
                                        }
                                    }
                                    queue.add("\n");
                                    queue.notify();
                                    announcedHosts.clear();
                                }
                            }
                        }
                    }
                }
            }, 0, PERIOD);
            while (true) {
                try {
                    MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
                    InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                    socket.joinGroup(group);

                    DatagramPacket packet;
                    while (true) {
                        byte[] buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        PacketInfo packetInfo = new PacketInfo(packet);
                        synchronized (missedPackets) {
                            synchronized (announcedHosts) {
                                announcedHosts.add(packetInfo.host);
                            }
                            if (!missedPackets.containsKey(packetInfo.host)) {
                                missedPackets.put(packetInfo.host, 0);
                            }
                            synchronized (macAddresses) {
                                macAddresses.put(packetInfo.host, packetInfo.macAddress);
                            }
                        }
                        synchronized (queue) {
                            queue.add("Received packet:");
                            queue.add("MAC address = " + packetInfo.macAddress);
                            queue.add("Host = " + packetInfo.host);
                            queue.add("Unix time = " + packetInfo.unixTimeDate);
                            queue.notify();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        executorService.execute(new Server());
        executorService.execute(new Logger(System.out));
        executorService.execute(new Client());
    }
}
