package ru.georgeee.itmo.sem7.networks.lab1;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


@Component
public class Monitor {
    private final static Logger log = LoggerFactory.getLogger(Monitor.class);

    @Autowired
    private Settings settings;

    public void run(ConcurrentMap<Long, Pair<Long, Message>> lastReceived) {
        Map<Long, Message> alive = new HashMap<>();
        while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            long currentTime = System.currentTimeMillis();

            for (Pair<Long, Message> pair : lastReceived.values()) {
                long timestamp = pair.getLeft();
                Message msg = pair.getRight();
                if (currentTime - timestamp > settings.getMissedThreshold() * settings.getInterval()) {
                    alive.remove(msg.getMacAddress());
                } else {
                    alive.put(msg.getMacAddress(), msg);
                }
            }

            StringBuilder sb = new StringBuilder("Alive: ");
            for (Message msg : alive.values()) {
                sb.append("{")
                        .append(msg.getHostName())
                        .append(" 0x").append(Long.toHexString(msg.getMacAddress()))
                        .append("}");
            }
            System.out.println(sb);

            try {
                Thread.sleep(settings.getInterval() * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}

