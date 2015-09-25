package ru.aleynikov.net.lab1;

import java.text.SimpleDateFormat;
import java.util.Map;

public class Output implements Runnable {
    public static final long TO_SLEEP = 2500;

    @Override
    public void run() {
        while (true) {
            try {
                Map<String, Info.SingleInfo> infoMap = Info.INSTANCE.getInfoMap();
                for (Map.Entry<String, Info.SingleInfo> entry : infoMap.entrySet()) {
                    Info.SingleInfo si = entry.getValue();
                    if (System.currentTimeMillis() - si.getLastReceive() > Info.BREAK_TIME) {
                        si.incMissedCount();
                    }
                    if (si.getMissedCount() >= Info.COUNT_TO_DELETE) {
                        infoMap.remove(entry.getKey());
                        continue;
                    }
                    System.out.println("MAC: " + entry.getKey()
                            + " | Hostname: " + si.getHostname()
                            + " | LastTimestamp: " + si.getLastSendTime()
                            + " | Last receive: " + new SimpleDateFormat("HH:mm:ss").format(si.getLastReceive())
                            + " | Missed: " + si.getMissedCount());
                }
                for (int i = 0; i < 70; ++i) {
                    System.out.print("-_");
                }
                System.out.println("-");
                Thread.sleep(TO_SLEEP);
            } catch (InterruptedException e) {
                System.err.println(e);
                System.exit(1);
            }
        }
    }
}
