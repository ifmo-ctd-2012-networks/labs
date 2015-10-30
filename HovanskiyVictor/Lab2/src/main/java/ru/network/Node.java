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

    public Node(String hostname, int port, String macAddress) {
        this.hostname = hostname;
        this.port = port;
        this.macAddress = macAddress;
    }

    public Node() {

    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[hostname=" + hostname + ", port=" + port + ", macAddress=" + macAddress + "]";
    }
}
