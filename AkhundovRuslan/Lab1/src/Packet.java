import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author korektur
 *         18/09/2015
 */
public class Packet {

    public final String macAddress;
    public final String hostName;
    public final int timeStamp;

    public Packet(DatagramPacket packet) {
        byte[] response = packet.getData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X%s", response[i], (i < 6 - 1) ? "-" : ""));
        }
        macAddress = sb.toString();

        byte hostNameLength = response[6];

        hostName = new String(Arrays.copyOfRange(response, 7, 7 + hostNameLength));

        byte[] timestampBytes = new byte[4];
        System.arraycopy(response, 7 + hostNameLength, timestampBytes, 0, 4);

        ByteBuffer buffer = ByteBuffer.wrap(timestampBytes);
        timeStamp = buffer.getInt();
    }

    @Override
    public String toString() {
        return "Packet{" +
                "macAddress='" + macAddress + '\'' +
                ", hostName='" + hostName + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
