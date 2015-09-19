package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class Client implements Runnable {
    private static final int BUFFER_SIZE = 128;
    private static final int MAX_PACKETS_MISSED = 5;
    private final int port;

    private final Set<String> currentInstances = new HashSet<>();
    private final Map<String, Integer> missedPackets = new HashMap<>();
    private final Map<String, String> instances = new HashMap<>();

    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        createCleaner();
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                socket.receive(packet);

                Message message = Message.fromByteArray(packet.getData());
                Logger.commit("Client:", String.format("Message: \'%s\' received", message.toString()));
                synchronized (missedPackets) {
                    synchronized (currentInstances) {
                        currentInstances.add(message.getMac());
                    }
                    if (!missedPackets.containsKey(message.getMac())) {
                        missedPackets.put(message.getMac(), 0);
                    }
                    synchronized (instances) {
                        instances.put(message.getMac(), message.getHost());
                    }
                }
                //TODO ADD LOGGER

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCleaner() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.commit("Client: Cleaner:", "Clean started");
                synchronized (missedPackets) {
                    synchronized (currentInstances) {
                        synchronized (instances) {
                            ArrayList<String> sortedHosts = new ArrayList<>(missedPackets.keySet());
                            Collections.sort(sortedHosts);
                            for (String s : sortedHosts) {
                                int missed = missedPackets.get(s);
                                if (!currentInstances.contains(s)) {
                                    missed++;
                                }
                                if (missed == MAX_PACKETS_MISSED) {
                                    Logger.commit("Client: Cleaner:", String.format("Instance (Hostname: %s, MAC-address: %s) missed too many packets. Removed", instances.get(s), s));
                                    missedPackets.remove(s);
                                    instances.remove(s);
                                } else {
                                    Logger.commit("Client: Cleaner:", String.format("Instance (Hostname: %s, MAC-address: %s) missed %d packets", instances.get(s), s, missed));
                                    missedPackets.put(s, missed);
                                }
                            }
                            currentInstances.clear();
                        }
                    }
                }
                Logger.commit("Client: Cleaner:", "Clean finished");
            }
        }, 0, 5000);
    }
}