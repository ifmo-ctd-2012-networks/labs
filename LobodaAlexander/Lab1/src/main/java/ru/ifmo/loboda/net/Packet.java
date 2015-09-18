package ru.ifmo.loboda.net;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Packet {
    protected final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private byte[] mac;
    private byte lenght;
    private byte[] name;
    private byte[] ts;

    public Packet(byte[] mac, String name, long ts) {
        if(mac == null){
            throw new IllegalArgumentException();
        }
        this.mac = Arrays.copyOfRange(mac, 0, mac.length);
        if(name == null){
            throw new IllegalArgumentException();
        }
        this.name = name.getBytes(Charset.forName("UTF-8"));
        if(this.name.length > 0xFF){
            throw new IllegalArgumentException();
        }
        lenght = (byte)this.name.length;
        ts /= 1000;
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt((int)ts);
        this.ts = buf.array();
    }

    public Packet(byte[] bytes) throws BadPacketException {
        if(bytes.length < 11){
            throw new BadPacketException("Packet too little");
        }
        mac = Arrays.copyOfRange(bytes, 0, 6);
        lenght = bytes[6];
        if(bytes.length != 11 + lenght){
            throw new BadPacketException("Packet has wrong size");
        }
        name = Arrays.copyOfRange(bytes, 7, 7 + lenght);
        ts = Arrays.copyOfRange(bytes, bytes.length - 4, bytes.length);
    }

    public byte[] getBytes(){
        byte[] result = new byte[mac.length + 1 + name.length + ts.length];
        System.arraycopy(mac, 0, result, 0, 6);
        result[6] = lenght;
        System.arraycopy(name, 0, result, 7, name.length);
        System.arraycopy(ts, 0, result, 7 + name.length, 4);
        return result;
    }

    public String getName(){
        return new String(name, Charset.forName("UTF-8"));
    }

    public long getTS(){
        ByteBuffer buf = ByteBuffer.wrap(ts);
        buf.order(ByteOrder.BIG_ENDIAN);
        int ts = buf.getInt();
        return ts & 0x00000000ffffffffL;
    }

    public byte[] getMAC(){
        return Arrays.copyOfRange(mac, 0, 6);
    }

    public static String MACAsString(byte[] mac){
        String res = "";
        for(int i = 0; i < mac.length; i++){
            res += hexArray[(mac[i] & 0xFF) / 16];
            res += hexArray[(mac[i] & 0xFF) % 16];
            if(i != mac.length - 1){
                res += "::";
            }
        }
        return res;
    }
}
