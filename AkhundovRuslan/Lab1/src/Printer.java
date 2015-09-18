import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author korektur
 *         18/09/2015
 */
public class Printer implements Runnable {
    public static final Logger LOG = Logger.getLogger(Printer.class.getName());
    private static final int TIME_TO_SLEEP = 5000;

    private ConcurrentLinkedQueue<Packet> packets;
    private Map<String, Node> nodes;
    private HashSet<String> updated;

    public Printer(ConcurrentLinkedQueue<Packet> packets) {
        this.packets = packets;
        nodes = new HashMap<>();
        updated = new HashSet<>();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long startTime = System.currentTimeMillis();
            updated.clear();
            while (!packets.isEmpty() && packets.peek().timeStamp < startTime) {
                Packet packet = packets.poll();
                updated.add(packet.macAddress);
                Node node = nodes.get(packet.macAddress);
                if (node != null) {
                    node.lostPackets = 0;
                } else {
                    nodes.put(packet.macAddress, new Node(packet));
                }
            }

            nodes.values().stream().forEach(node -> {if (!updated.contains(node.macAddress)) ++node.lostPackets;});

            nodes = nodes.entrySet().stream().filter(e -> e.getValue().lostPackets < 5)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            nodes.values().forEach(System.out::println);

            while (System.currentTimeMillis() - startTime < TIME_TO_SLEEP) {
                try {
                    Thread.sleep(TIME_TO_SLEEP - (System.currentTimeMillis() - startTime));
                } catch (InterruptedException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
}
