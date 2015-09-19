package ru.aleynikov.net.lab1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Info {
    protected final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static long BREAK_TIME = 5069;
    public static int COUNT_TO_DELETE = 5;
    public static final Info INSTANCE = new Info();
    private Map<String, SingleInfo> mMap;

    private Info() {
        mMap = new ConcurrentHashMap<>();
    }

    public Map<String, SingleInfo> getInfoMap() {
        return mMap;
    }

    public static String MacAsString(byte[] mac) {
        String res = "";
        for (int i = 0; i < mac.length; i++) {
            res += hexArray[(mac[i] & 0xFF) / 16];
            res += hexArray[(mac[i] & 0xFF) % 16];
            if (i != mac.length - 1) {
                res += "::";
            }
        }
        return res;
    }

    public synchronized void updateInfo (Message si) {
        String macString = MacAsString(si.getMac());
        if (mMap.containsKey(macString)) {
            SingleInfo prev = mMap.get(macString);
            if (prev.getMissedCount() >= COUNT_TO_DELETE) {
                mMap.remove(macString);
            } else {
                prev.setLastReceive(System.currentTimeMillis());
                prev.setLastSendTime(si.getTS());
            }
        } else {
            mMap.put(macString, new SingleInfo(System.currentTimeMillis(), si.getTS(), si.getName()));
        }
    }

    public class SingleInfo {
        private long lastReceive;
        private long lastSendTime;
        private int missedCount;
        private String hostname;

        SingleInfo (long lastReceive, long lastSendTime, String hostname) {
            this.lastReceive = lastReceive;
            this.hostname = hostname;
            this.lastSendTime = lastSendTime;
            missedCount = 0;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public long getLastSendTime() {
            return lastSendTime;
        }

        public void setLastSendTime(long lastSendTime) {
            this.lastSendTime = lastSendTime;
        }

        public int getMissedCount() {
            return missedCount;
        }

        public long getLastReceive() {
            return lastReceive;
        }

        public void incMissedCount() {
            ++missedCount;
        }

        public void setLastReceive(long lastReceive) {
            this.lastReceive = lastReceive;
        }
    }

    public static class Message {
        private static final int TS_SIZE = Long.SIZE / Byte.SIZE;
        private byte[] mac;
        private byte lenght;
        private byte[] name;
        private byte[] ts;

        public Message (byte[] mac, String name, long ts) {
            if (mac == null || name == null){
               throw new IllegalArgumentException();
            }
            this.mac = Arrays.copyOfRange(mac, 0, mac.length);
            this.name = name.getBytes(Charset.forName("UTF-8"));
            if (this.name.length > 0xFF){
                throw new IllegalArgumentException();
            }
            this.lenght = (byte)this.name.length;
            this.ts = ByteBuffer.allocate(TS_SIZE).order(ByteOrder.BIG_ENDIAN).putLong(ts/1000).array();
        }

        public Message (byte[] bytes) throws BadInfoException {
            if (bytes.length < 7 + TS_SIZE){
                throw new BadInfoException("Too little array");
            }
            mac = Arrays.copyOfRange(bytes, 0, 6);
            lenght = bytes[6];
            if (bytes.length != 7 + TS_SIZE + lenght){
                throw new BadInfoException("Wrong size of array");
            }
            name = Arrays.copyOfRange(bytes, 7, 7 + lenght);
            ts = Arrays.copyOfRange(bytes, bytes.length - TS_SIZE, bytes.length);
        }

        public byte[] getBytes(){
            byte[] result = new byte[7 + TS_SIZE + name.length];
            System.arraycopy(mac, 0, result, 0, 6);
            result[6] = lenght;
            System.arraycopy(name, 0, result, 7, name.length);
            System.arraycopy(ts, 0, result, 7 + name.length, TS_SIZE);
            return result;
        }

        public String getName(){
            return new String(name, Charset.forName("UTF-8"));
        }

        public long getTS(){
            ByteBuffer buf = ByteBuffer.wrap(ts);
            buf.order(ByteOrder.BIG_ENDIAN);
            return buf.getLong();
        }

        public byte[] getMac(){
            return Arrays.copyOfRange(mac, 0, 6);
        }

    }

    public static class BadInfoException extends Exception {
        public BadInfoException(String message) {
            super(message);
        }
    }
}
