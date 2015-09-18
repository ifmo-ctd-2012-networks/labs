import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Charm on 16/09/15.
 */
public class MacAddress {
    public static byte[] getMacAdress() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                return null;
            }
            return network.getHardwareAddress();
        } catch (UnknownHostException | SocketException e) {
            System.out.println(e + " " + e.getMessage());
        }
        return null;
    }

}
