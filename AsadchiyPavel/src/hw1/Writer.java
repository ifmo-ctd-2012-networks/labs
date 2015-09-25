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
            long currentTime = System.currentTimeMillis() / 1000;
            while (!messages.isEmpty() && (messages.peek().getTime() == null
                    || messages.peek().getTime().getTime() / 1000 < currentTime)) {
                messagesForPrint.remove(messages.peek());
                messagesForPrint.add(messages.peek());
                messages.poll();
            }
            Iterator<ReceivedInfo> iterator = messagesForPrint.iterator();
            boolean wasPrint = false;
            System.out.println("[");
            while (iterator.hasNext()) {
                ReceivedInfo info = iterator.next();
                if (info.getTime().getTime() / 1000 < currentTime - Main.millisecondsSleepWriter / 1000 ||
                        info.isIncorrectData()) {
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
