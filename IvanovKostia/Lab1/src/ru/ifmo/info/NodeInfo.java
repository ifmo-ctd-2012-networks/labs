package ru.ifmo.info;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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

    public static NodeInfo atNetworkInterface(NetworkInterface networkInterface) throws SocketException {
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
        return null;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", hostname, mac);
    }

    @Override
    public int compareTo(NodeInfo o) {
        return mac.compareTo(o.mac);
    }
}
