package lab1;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            Server server = new Server(port);
            Client client = new Client(port);
            Runner runner = new Runner(server);
            server.start();
            client.start();
            runner.start();
            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
            }
            runner.close();
            client.close();
            server.close();
        } catch (SocketException e) {
            System.err.println("Error while initializing server: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Error while initializing client: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error while parsing port number: " + e.getMessage());
        }
    }

    private static class Runner extends Thread {
        private final Server server;
        private final Map<Long, Record> records = new HashMap<>();
        private volatile boolean stopped;

        public Runner(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            while (!stopped) {
                boolean changed = true;
                List<Message> messages = server.getMessages();
                for (Message message : messages) {
                    if (records.containsKey(getMac(message.mac))) {
                        Record r = records.get(getMac(message.mac));
                        r.timeStamp = Math.max(r.timeStamp, message.timeStamp);
                    } else {
                        records.put(getMac(message.mac), new Record(message.mac, message.hostName, message.timeStamp));
                        changed = true;
                    }
                }
                List<Long> toRemove = new ArrayList<>();
                long time = System.currentTimeMillis() / 1000l;
                for (long s : records.keySet()) {
                    if (records.get(s).timeStamp < time - 25) {
                        toRemove.add(s);
                        changed = true;
                    }
                }
                toRemove.forEach(records::remove);
                if (changed) {
                    List<Record> rec = new ArrayList<>();
                    rec.addAll(records.values());
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
                    System.out.println("Time: " + System.currentTimeMillis() / 1000l);
                    rec.forEach(System.out::println);
                    System.out.println("--------------------------------------");
                }
                time = System.currentTimeMillis();
                while (!stopped && System.currentTimeMillis() < time + 5000) {
                    try {
                        Thread.sleep(time + 5000 - System.currentTimeMillis());
                    } catch (InterruptedException e) {
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

        private static class Record {
            public byte[] mac;
            public String hostName;
            public long timeStamp;

            public Record(byte[] mac, String hostName, long timeStamp) {
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
                return result.substring(0, result.length() - 1) + " | " + hostName + " | Last seen: " + timeStamp;
            }
        }
    }
}
