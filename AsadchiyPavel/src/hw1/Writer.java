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
        while (!Thread.interrupted()) {
            long currentTimeMillis = System.currentTimeMillis();
            while (!messages.isEmpty() && messages.peek().getTime().getTime() < currentTimeMillis) {
                messagesForPrint.remove(messages.peek());
                messagesForPrint.add(messages.peek());
                messages.poll();
            }
            Iterator<ReceivedInfo> iterator = messagesForPrint.iterator();
            boolean wasPrint = false;
            System.out.println("[");
            while (iterator.hasNext()) {
                ReceivedInfo info = iterator.next();
                if (info.getTime().getTime() < currentTimeMillis - Main.millisecondsSleepWriter) {
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
