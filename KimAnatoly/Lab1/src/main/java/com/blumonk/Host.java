package com.blumonk;

/**
 * @author akim
 */
public class Host {

    private String mac;
    private String hostname;

    public Host(byte[] mac, byte[] hostname) {
        this.mac = Utils.macToString(mac);
        this.hostname = Utils.bytesToString(hostname);
    }

    public Host(Packet packet) {
        mac = packet.getMac();
        hostname = packet.getHostname();
    }

    public String getMac() {
        return mac;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return hostname + " (" + mac + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        if (mac != null ? !mac.equals(host.mac) : host.mac != null) return false;
        return !(hostname != null ? !hostname.equals(host.hostname) : host.hostname != null);

    }

    @Override
    public int hashCode() {
        int result = mac != null ? mac.hashCode() : 0;
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        return result;
    }
}
