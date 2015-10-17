import java.util.*;

/**
 * Created by 1 on 25.09.2015.
 */
public class Controller extends Thread {
    private final Server server;
    private final Map<Long, Dude> monitor = new HashMap<>();
    private volatile boolean stopped;

    public Controller(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            List<Message> messages = server.getMessages();
            for (Message message : messages) {
                if (monitor.containsKey(getMac(message.mac))) {
                    Dude r = monitor.get(getMac(message.mac));
                    r.timeStamp = Math.max(r.timeStamp, message.timeStamp);
                } else {
                    monitor.put(getMac(message.mac), new Dude(message.mac, message.hostName, message.timeStamp));
                }
            }
            long time = System.currentTimeMillis() / 1000L;
            List<Long> toDel = new ArrayList<>();
            for (long s : monitor.keySet()) {
                if (monitor.get(s).timeStamp < time - 25) {
                    toDel.add(s);
                    System.out.println(monitor.get(s).hostName + " was deleted");
                }
            }
            toDel.forEach(monitor::remove);
            List<Dude> rec = new ArrayList<>();
            rec.addAll(monitor.values());
            Collections.sort(rec, (o1, o2) -> {
                for (int i = 0; i < 6; i++) {
                    int a = ((int) o1.mac[i] + 256) % 256;
                    int b = ((int) o2.mac[i] + 256) % 256;
                    if (a < b) {
                        return -1;
                    } else if (a > b) {
                        return 1;
                    }
                }
                return 0;
            });
//            System.out.println("Current Time: " + System.currentTimeMillis() / 1000L);
            rec.forEach(System.out::println);
            System.out.println("**************************************************");
            time = System.currentTimeMillis();
            while (!stopped && System.currentTimeMillis() < time + 5000) {
                try {
                    Thread.sleep(time + 5000 - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    if (stopped)
                        return;
                }
            }
        }
    }

    private long getMac(byte[] b) {
        long result = 0;
        for (int i = 0; i < 6; i++) {
            result += ((int) b[i]) << 16 * i;
        }
        return result;
    }

    public void close() {
        stopped = true;
        interrupt();
    }

    private static class Dude {
        public byte[] mac;
        public String hostName;
        public long timeStamp;

        public Dude(byte[] mac, String hostName, long timeStamp) {
            this.mac = mac;
            this.hostName = hostName;
            this.timeStamp = timeStamp;
        }

        @Override
        public String toString() {
            String result = "";
            for (byte b : mac) {
                result += String.format("%02X:", b);
            }
            return result.substring(0, result.length() - 1) + " | " + hostName + " | " + timeStamp;
        }
    }
}
