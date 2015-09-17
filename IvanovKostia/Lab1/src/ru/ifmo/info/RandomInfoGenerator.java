package ru.ifmo.info;

import java.util.Iterator;
import java.util.Random;

public class RandomInfoGenerator implements Iterator<NodeInfo> {
    private Random random = new Random();
    private int id = 1;

    public NodeInfo next() {
        int[] ints = random.ints()
                .limit(MacAddress.SIZE)
                .toArray();
        byte[] bytes = new byte[MacAddress.SIZE];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) ints[i];
        }

        return new NodeInfo(new MacAddress(bytes), String.format("Host #%d", id++));
    }

    @Override
    public boolean hasNext() {
        return true;
    }
}
