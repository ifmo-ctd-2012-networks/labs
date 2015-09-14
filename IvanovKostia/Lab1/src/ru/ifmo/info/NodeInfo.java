package ru.ifmo.info;

import ru.ifmo.broadcast.MacAddress;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NodeInfo implements Comparable<NodeInfo> {
    private final MacAddress mac;
    private final String hostname;

    public NodeInfo(MacAddress mac, String hostname) {
        this.mac = mac;
        this.hostname = hostname;
    }

    public MacAddress getMacAdress() {
        return mac;
    }

    public String getHostname() {
        return hostname;
    }

    public Message toMessage() {
        return new Message(this);
    }

    public static NodeInfo makeLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress != null) {
                    MacAddress mac = new MacAddress(hardwareAddress);
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    if (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        String hostName = inetAddress.getHostName();
                        return new NodeInfo(mac, hostName);
                    }
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "mac=" + mac +
                ", hostname='" + hostname + '\'' +
                '}';
    }

    @Override
    public int compareTo(NodeInfo o) {
        return mac.compareTo(o.mac);
    }
}
