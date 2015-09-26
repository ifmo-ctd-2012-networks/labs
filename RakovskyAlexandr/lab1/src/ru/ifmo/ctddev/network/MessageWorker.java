package ru.ifmo.ctddev.network;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageWorker implements Runnable {

    private BlockingQueue<Message> msgs;
    private Map<String, HostPerson> hosts;
    private SortedSet<HostPerson> sortedHosts;

    public MessageWorker(BlockingQueue<Message> msgs) {
        this.msgs = msgs;
        this.hosts = new HashMap<>();
        this.sortedHosts = new TreeSet<>(new HostComparator());
    }

    @Override
    public void run() {
        while (true) {
            Message msg;
            try {
                msg = msgs.poll(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            long current = System.currentTimeMillis();
            HostPerson host = null;
            if (msg != null) {
                host = hosts.get(msg.getMac());
                if (host == null) {
                    host = new HostPerson(msg.getMac(), msg.getHostnameLength(), msg.getHostname(), msg.getTimestamp(), current);
                    hosts.put(host.getMac(), host);
                }
            }

            for (HostPerson h : hosts.values()) {
                if (host != null && h.getMac().equals(host.getMac())) {
                    h.updateHistory(current, true);
                } else {
                    h.updateHistory(current, false);
                }
            }

            sortedHosts.clear();
            sortedHosts.addAll(hosts.values());

            print(current);
        }
    }

    public void print(long current) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("====================================================" +
                "=====================================================" + "\n");

        Iterator<HostPerson> it = sortedHosts.iterator();
        while (it.hasNext()) {
            HostPerson host = it.next();
            if (current - host.getLastReceived() >= 5000 || host.getPacketsLost() == 5) {
                it.remove();
                hosts.remove(host.getMac());
            } else {
                sb.append(host.toString()).append("\n");
            }
        }
        System.out.println(sb.toString());
    }

    public class HostComparator implements Comparator<HostPerson> {
        @Override
        public int compare(HostPerson o1, HostPerson o2) {
            if (o1.getPacketsLost() != o2.getPacketsLost()) {
                return Integer.compare(o1.getPacketsLost(), o2.getPacketsLost());
            }
            return o1.getMac().compareTo(o2.getMac());
        }
    }
}
