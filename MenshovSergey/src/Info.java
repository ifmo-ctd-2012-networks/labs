import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by sergej on 14.09.15.
 */
public class Info {
    public static byte[] getMacAddr() {
        try {
            NetworkInterface network = NetworkInterface.getByName("wlan0");
            return network.getHardwareAddress();

        } catch (SocketException e) {
            e.printStackTrace();

        }
        return null;
    }
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
