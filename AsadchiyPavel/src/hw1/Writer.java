package hw1;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class Writer implements Runnable {
    private static final Logger log = Logger.getLogger(Writer.class.getName());

    private final ConcurrentLinkedQueue<ReceivedInfo> messages = new ConcurrentLinkedQueue<>();
    private final SortedSet<ReceivedInfo> messagesForPrint = new TreeSet<>();

    public void add(ReceivedInfo receivedInfo) {
        messages.add(receivedInfo);
    }

    @Override
    public void run() {
        long delimiter = (Server.isIntTime ? 1000 : 1);
        while (!Thread.interrupted()) {
            long currentTime = System.currentTimeMillis() / delimiter;
            while (!messages.isEmpty() && messages.peek().getTime().getTime() / delimiter < currentTime) {
                messagesForPrint.remove(messages.peek());
                messagesForPrint.add(messages.peek());
                messages.poll();
            }
            Iterator<ReceivedInfo> iterator = messagesForPrint.iterator();
            boolean wasPrint = false;
            System.out.println("[");
            while (iterator.hasNext()) {
                ReceivedInfo info = iterator.next();
                if (info.getTime().getTime() / delimiter < currentTime - Main.millisecondsSleepWriter / delimiter) {
                    info.incCounter();
                }
                if (info.getCounter() == Main.maxMissPackets) {
                    iterator.remove();
                } else {
                    System.out.print(wasPrint ? ",\n" : "");
                    System.out.print("\t" + info);
                    wasPrint = true;
                }
            }
            System.out.println(wasPrint ? "\n]" : "\tNothing\n]");
            try {
                Thread.sleep(Main.millisecondsSleepWriter);
            } catch (InterruptedException e) {
                log.severe("Thread sleep was interrupted, error message: " + e.getMessage());
                return;
            }
        }
    }
}
