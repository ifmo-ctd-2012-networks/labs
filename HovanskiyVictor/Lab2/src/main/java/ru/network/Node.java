package ru.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author victor
 */
public class Node {
    protected InetAddress address;
    protected String hostname;
    protected int port;
    protected String macAddress;
    private boolean active;
    private long timestamp;

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(address, port);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValid() {
        return address != null && macAddress != null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[hostname=" + hostname + ", active=" + active + "]";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
