package com.lukin.network.lab1;

import java.util.Arrays;

/**
 * Created by Саша on 18.09.2015.
 */
public class MACAddress implements Comparable<MACAddress> {
    private final byte[] mac;

    public MACAddress(byte[] mac) throws Exception {
        if (mac.length == 6) {
            this.mac = mac;
        } else throw new Exception("Invalid MAC address");
    }

    public byte[] getBytes(){
        return mac;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MACAddress)) return false;

        MACAddress that = (MACAddress) o;

        if (!Arrays.equals(mac, that.mac)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mac);
    }

    @Override
    public int compareTo(MACAddress o) {
        for (int i = 0; i < 6; i++) {
            if (this.mac[i] > o.mac[i])
                return 1;
            if (this.mac[i] < o.mac[i])
                return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "")); // X - hexadecimal integer
        }
        return sb.toString();
    }
}
