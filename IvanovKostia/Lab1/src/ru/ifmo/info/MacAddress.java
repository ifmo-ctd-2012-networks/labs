package ru.ifmo.info;

import java.util.Arrays;

public class MacAddress implements Comparable<MacAddress> {
    public static final int SIZE = 6;

    public final byte[] value;

    public MacAddress(byte[] value) {
        if (value.length != SIZE)
            throw new IllegalStateException(String.format("Accept only byte arrays of length %d, but %d was given", SIZE, value.length));
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            sb.append(String.format("%02X%s", value[i], (i < value.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

    @Override
    public int compareTo(MacAddress other) {
        byte[] otherValue = other.value;
        for (int i = 0; i < value.length; i++) {
            if (value[i] != otherValue[i]) return Byte.compare(value[i], otherValue[i]);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MacAddress that = (MacAddress) o;

        return Arrays.equals(value, that.value);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
