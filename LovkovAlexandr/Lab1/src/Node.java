import java.util.Date;

/**
 * Created by Charm on 17/09/15.
 */
public class Node {

    private String macAddress;
    private String host;
    private boolean was;
    private int lostPackets;
    private Date timestamp;

    Node(String macAddress, String host, boolean was, Date timestamp) {
        this.macAddress = macAddress;
        this.host = host;
        this.was = was;
        this.lostPackets = 0;
        this.timestamp = timestamp;
    }

    public String getHost() {
        return host;
    }

    public boolean getWas() {
        return was;
    }

    public int getLostPackets() {
        return lostPackets;
    }

    public Date getTimestamp() {
        return (Date)timestamp.clone();
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setLostPackets(int lostPackets) {
        this.lostPackets = lostPackets;
    }

    public void setWas(boolean was) {
        this.was = was;
    }


}
