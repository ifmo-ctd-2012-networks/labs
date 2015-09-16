package ru.ifmo.info;

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
        return new LocalInfoGenerator().next();
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
