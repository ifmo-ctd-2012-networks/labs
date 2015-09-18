import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Created by Charm on 15/09/15.
 */
public class Printer implements Runnable {

    ConcurrentLinkedQueue<Node> queue;
    Map<String, Node> hashMap;

    Printer(ConcurrentLinkedQueue<Node> queue) {
        this.queue = queue;
        hashMap = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            long startTime = System.currentTimeMillis();
            long timeFinish = startTime + 5000;
            System.out.println(queue.size());
            while (!queue.isEmpty() && queue.peek().getTimestamp().getTime() < timeFinish) {
                Node node = queue.peek();
                Node tmp = hashMap.get(node.getHost());
                if (tmp != null) {
                    node.setLostPackets(0);
                }
                hashMap.put(node.getHost(), node);
                queue.poll();
            }
            print();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void print() {
        System.out.println();
        //System.out.println("!!!!!!!!");
        for (Map.Entry<String, Node> entry : hashMap.entrySet()) {
            if (!entry.getValue().getWas()) {
                entry.getValue().setLostPackets(entry.getValue().getLostPackets() + 1);
            }
        }
        hashMap = hashMap.entrySet().stream().filter(e -> e.getValue().getLostPackets() < 5).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        /*
        TreeSet<Node> treeSet = hashMap.values().stream().collect(Collectors.toCollection(TreeSet::new));
        for (Node node:treeSet) {

        }
        */

        for (Map.Entry<String, Node> entry : hashMap.entrySet()) {
            String macAddress = entry.getKey();
            Node value = entry.getValue();
            value.setWas(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value.getTimestamp());
            String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" +
                    calendar.get(Calendar.SECOND);
            System.out.print(value.getHost() + " " + time + " lost=" + +value.getLostPackets() + " | ");
        }
    }
}
