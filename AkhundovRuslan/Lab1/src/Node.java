/**
 * @author korektur
 *         18/09/2015
 */
public class Node {

    public int lostPackets;
    public String hostname;
    public final String macAddress;

    public Node(Packet packet) {
        this(packet.hostName, packet.macAddress);
    }

    public Node(String hostname, String macAddress) {
        this.hostname = hostname;
        this.macAddress = macAddress;
        lostPackets = 0;
    }

    @Override
    public String toString() {
        return "Node{" +
                "lostPackets=" + lostPackets +
                ", hostname='" + hostname + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
