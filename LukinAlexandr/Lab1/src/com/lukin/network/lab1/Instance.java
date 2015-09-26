package com.lukin.network.lab1;

/**
 * Created by Саша on 19.09.2015.
 */
public class Instance {
    private final MACAddress macAddress;
    private final String hostname;
    private int missedPackets;
    private boolean valid;

    public Instance(MACAddress macAddress, String hostname) {
        this.macAddress = macAddress;
        this.hostname = hostname;
    }

    public MACAddress getMacAddress() {
        return macAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
        if (valid)
            missedPackets = 0;
    }

    public int getMissedPackets(){
        return missedPackets;
    }

    public void packetLost(){
        missedPackets++;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "macAddress=" + macAddress +
                ", hostname='" + hostname +
                ", missedPackets=" + missedPackets +
                '}';
    }
}
