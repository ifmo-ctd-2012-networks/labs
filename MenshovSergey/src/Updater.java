import network.BroadcastReceiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sergej on 17.09.15.
 */
public class Updater implements Runnable{
    private ConcurrentHashMap<String, Long> instances;
    private final Map<String, Long> countSkips = new HashMap<>();
    public Updater(BroadcastReceiver receiver) {
        instances = receiver.getInstances();
    }

    @Override
    public void run() {
        Set<String> delete = new HashSet<>();
        while (true) {
            for (String comp : instances.keySet()) {
                long time = instances.get(comp);
                if (countSkips.containsKey(comp)) {
                    countSkips.put(comp, time);

                }
            }
            long curTime = System.currentTimeMillis();
            for (String comp : instances.keySet()) {
                long lastTime = instances.get(comp);
                if (curTime >= lastTime + 25000) {
                    delete.add(comp);
                }
            }
            for (String del : delete) {
                instances.remove(del);
            }
            delete.clear();
            for (String cur : instances.keySet()) {
                System.out.println(cur + " last time = " + instances.get(cur));
            }
            if (instances.size() == 0) {
                System.out.println("empty seeet");
            }
            System.out.println("end set.......");
            System.out.println("\n \n");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
