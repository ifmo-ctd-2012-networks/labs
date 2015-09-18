package network;

/**
 * Created by sergej on 17.09.15.
 */
public class Message {
    private final byte[] macAddr;
    private final String hostName;
    private final long time;

    public Message(byte[] macAddr, String hostName, long time) {

        this.macAddr = macAddr;
        this.hostName = hostName;
        this.time = time;
    }
    @Override
    public String toString() {
        return getMacaddr(macAddr) + " " + hostName + " " +time;
    }
    private String getMacaddr(byte[] message) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X:",message[i]));
        }
        return sb.toString();
    }
}
