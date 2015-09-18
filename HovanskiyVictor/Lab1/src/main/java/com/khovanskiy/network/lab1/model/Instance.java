package com.khovanskiy.network.lab1.model;

/**
 * @author victor
 */
public class Instance {
    private final MacAddress macAddress;
    private final String hostname;
    private int missedPackets;
    private boolean actual;

    public Instance(MacAddress macAddress, String hostname) {
        this.macAddress = macAddress;
        this.hostname = hostname;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isActual() {
        return actual;
    }

    public void setActual(boolean actual) {
        this.actual = actual;
        // Исключаем по суммарному количеству пропущенных пакетов или с последнего принятого пакета?
        if (actual) {
            missedPackets = 0;
        }
    }

    public void missPacket() {
        ++missedPackets;
    }

    public int getMissedPackets() {
        return missedPackets;
    }

    @Override
    public String toString() {
        return "Instance[macAddress=" + macAddress + ", hostname=" + hostname + ", missedPackets=" + missedPackets + "]";
    }
}
