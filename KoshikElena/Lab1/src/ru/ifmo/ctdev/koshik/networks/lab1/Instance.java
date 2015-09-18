package ru.ifmo.ctdev.koshik.networks.lab1;


public class Instance implements Comparable<Instance> {

    public final String macAddress;
    public final String hostname;
    private boolean receivedPacket;
    private int missedPackagesCount;


    public Instance(String macAddress, String hostname) {
        this.macAddress = macAddress;
        this.hostname = hostname;
        this.receivedPacket = false;
        missedPackagesCount = 0;
    }

    public void setReceivedPacket() {
        receivedPacket = true;
    }

    public boolean receivedPacket() {
        return receivedPacket;
    }

    public void resetReceivedPacket() {
        receivedPacket = false;
    }

    public int incMissedPacketsCount() {
        return ++missedPackagesCount;
    }

    @Override
    public int compareTo(Instance instance) {
        return macAddress.compareTo(instance.macAddress);
    }

    @Override
    public int hashCode() {
        return macAddress.hashCode();
    }
}
