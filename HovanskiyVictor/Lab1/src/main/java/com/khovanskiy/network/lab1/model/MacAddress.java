package com.khovanskiy.network.lab1.model;

/**
 * @author victor
 */
public class MacAddress {

    private final byte[] mac;

    public MacAddress(byte[] mac) {
        this.mac = mac;
    }

    public byte[] getBytes() {
        return mac;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; ++i) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }
}
