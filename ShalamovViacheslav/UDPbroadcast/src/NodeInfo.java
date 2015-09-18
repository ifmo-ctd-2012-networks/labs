import java.io.UnsupportedEncodingException;

/**
 * Created by viacheslav on 16.09.2015.
 */
public class NodeInfo implements Comparable {
    private byte[] mac;
    private String hostname;
    private int lastIteration;

    public NodeInfo(byte[] mac, String hostname, int iteration) {
        this.mac = mac;
        this.hostname = hostname;
        lastIteration = iteration;
    }

    @Override
    public int compareTo(Object other) {
        try {
            String thisMac = new String(mac, "UTF-8");
            String otherMac = ByteUtls.bytesToHex(((NodeInfo) other).getMac());
            return thisMac.compareTo(otherMac);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getLastIteration() {
        return lastIteration;
    }

    public void setLastIteration(int lastIteration) {
        this.lastIteration = lastIteration;
    }
}
