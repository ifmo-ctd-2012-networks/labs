package ru.ifmo.ctddev.efimova.net.udp;

import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

public class ServerTask extends TimerTask {

    private class HostLost {
        int lost;
        Message msg;

        public HostLost(Message msg, int lost) {
            this.lost = lost;
            this.msg = msg;
        }
    }

    private BlockingQueue<Message> msgs;
    private static Map<String, HostLost> hostByMac;

    public ServerTask(BlockingQueue<Message> msgs) {
        this.msgs = msgs;
        this.hostByMac = new TreeMap<String, HostLost>();
    }


    @Override
    public void run() {
        Message msg = null;
        int n = msgs.size();
        for (int i = 0; i < n; i++) {
            try {
                msg = msgs.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (msg != null) {
                HostLost hl = hostByMac.get(msg.mac);
                if (hl == null) {
                    hl = new HostLost(msg, 0);
                } else {
                    if (hl.lost > 0) {
                        hl.lost--;
                    }
                    hl.msg = msg;
                }
                hostByMac.put(msg.mac, hl);
            }
        }

        Iterator<Map.Entry<String, HostLost>> it = hostByMac.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, HostLost> entry = it.next();
            HostLost hl = entry.getValue();

            hl.lost++;

            if (hl.lost == 5) {
                it.remove();
                continue;
            }

            hostByMac.put(hl.msg.mac, hl);
            System.out.println(hl.msg.toString());
        }
        System.out.println("---------------------------------------------------------");
    }
}
