package com.khovanskiy.network.lab1.model;

import java.util.Arrays;

/**
 * @author victor
 */
public class MacAddress implements Comparable<MacAddress> {

    private final static int LENGTH = 6;
    private final byte[] mac;

    public MacAddress(byte[] mac) {
        assert mac.length == LENGTH;
        this.mac = mac;
    }

    public byte[] getBytes() {
        return mac;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mac);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MacAddress) {
            MacAddress other = (MacAddress) obj;
            return Arrays.equals(this.mac, other.mac);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; ++i) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

    @Override
    public int compareTo(MacAddress other) {
        for (int i = 0; i < LENGTH; ++i) {
            if (this.mac[i] < other.mac[i]) {
                return -1;
            } else if (this.mac[i] > other.mac[i]) {
                return 1;
            }
        }
        return 0;
    }
}
