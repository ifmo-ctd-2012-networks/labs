import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Date;

/**
 * @author korektur
 *         18/09/2015
 */
public class Packet {

    public final String macAddress;
    public final String hostName;
    public final long timeStamp;

    public Packet(DatagramPacket packet) {
        byte[] response = packet.getData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X%s", response[i], (i < 6 - 1) ? "-" : ""));
        }
        macAddress = sb.toString();

        byte hostNameLength = response[6];

        hostName = new String(Arrays.copyOfRange(response, 7, 7 + hostNameLength));

        long timeStamp = 0;
        for (int i = 7 + hostNameLength + 1; i < packet.getLength(); i++)
        {
            timeStamp = (timeStamp << 8) + (response[i] & 0xff);
        }
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "macAddress='" + macAddress + '\'' +
                ", hostName='" + hostName + '\'' +
                ", timeStamp=" + new Date(timeStamp) +
                '}';
    }
}
