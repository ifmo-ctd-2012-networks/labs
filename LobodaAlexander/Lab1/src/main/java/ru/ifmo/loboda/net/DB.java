package ru.ifmo.loboda.net;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class DB {
    private Map<String, Packet> last;
    private Map<String, Integer> missing;

    public DB(){
        last = new TreeMap<>();
        missing = new TreeMap<>();
    }

    public synchronized void update(Packet info) {
        String name = Packet.MACAsString(info.getMAC());
        missing.put(name, -1);
        last.put(name, info);
    }

    public synchronized void tick(PrintWriter writer){
        Set<String> toDelete = new TreeSet<>();
        int maxw = 0;
        for(String mac : last.keySet()){
            maxw = Math.max(maxw, last.get(mac).getName().length());
        }
        for(String mac : last.keySet()){
            if(missing.get(mac) == 4){
                toDelete.add(mac);
            } else {
                missing.put(mac, missing.get(mac) + 1);
                Packet packet = last.get(mac);
                writer.print(mac + " | ");
                writer.printf("%-" + maxw + "s", packet.getName());
                writer.println(" | " + packet.getTS() + " | " + missing.get(mac));
            }
        }
        for(String key : toDelete){
            last.remove(key);
            missing.remove(key);
        }
        writer.flush();
    }
}