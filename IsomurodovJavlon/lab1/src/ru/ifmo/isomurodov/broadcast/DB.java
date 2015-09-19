package ru.ifmo.isomurodov.broadcast;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by javlon on 18.09.15.
 */
public class DB {
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private Map<String, UDP> map;
    private Map<String, Integer> missing;

    public DB(){
        map = new TreeMap<>();
        missing = new TreeMap<>();
    }

    public synchronized void add(UDP info) {
        String name = "";
        byte[] mac= info.getMacAdress();
        for(int i = 0; i < mac.length; i++){
            name += hexArray[((int)mac[i] + 128) / 16];
            name += hexArray[((int)mac[i] + 128) % 16];
            if((i + 1)%2==0 && i != mac.length-1){
                name += ".";
            }
        }
        missing.put(name, -1);
        map.put(name, info);
    }

    public synchronized void tick(){
        Set<String> toDelete = new TreeSet<>();
        int maxw = 0;
        for(String mac : map.keySet()){
            maxw = Math.max(maxw, map.get(mac).getHostname().length());
        }
        for(String mac : map.keySet()){
            if(missing.get(mac) == 4){
                toDelete.add(mac);
            } else {
                missing.put(mac, missing.get(mac) + 1);
                UDP packet = map.get(mac);
                System.out.print("  " + mac + " | " + packet.getHostname());
                System.out.println(" | " + packet.getTimeStamp() + " | " + missing.get(mac));
            }
        }
        for(String key : toDelete){
            map.remove(key);
            missing.remove(key);
        }
    }
}
