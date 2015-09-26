import java.util.*;

/**
 * Created by Георгий on 26.09.2015.
 */
public class Printer extends  Thread {
    private  final Server server;
    private final Map<String, Packet> received = new HashMap<>();
    private final Map<String, Integer> missed = new HashMap<>();

    public Printer(Server server) {
        this.server = server;
    }

    @Override
    public void run(){
        while (true){
            List<Packet> packets = server.getPackets();
            for (Packet packet : packets){
                String address = packet.getMacAddress();
                missed.put(address, 0);
                received.put(address, packet);
            }

            Set<String> toRemove = new HashSet<>();
            for (String address : received.keySet()) {
                if (missed.get(address) >= 5) {
                    toRemove.add(address);
                } else {
                    missed.put(address, missed.get(address) + 1);
                    Packet packet = received.get(address);
                    System.out.println("macAddress = " + address + "| Missed = " + (missed.get(address) - 1) +"| hostName = " + packet.getName() + "| timestamp = " + packet.getTimestamp());
                }
            }
            System.out.println("KOL!!!!!!!!!!!!!!!!!!!!!!!!! = " + packets.size() + "MAP!!!!! =" + received.size());
            for (String delete : toRemove) {
                received.remove(delete);
                missed.remove(delete);
            }
            System.out.println("--------------------------------------");
            long time = System.currentTimeMillis();
            while (System.currentTimeMillis() < time + Client.SLEEP_TIME) {
                try {
                    Thread.sleep(time + Client.SLEEP_TIME - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
