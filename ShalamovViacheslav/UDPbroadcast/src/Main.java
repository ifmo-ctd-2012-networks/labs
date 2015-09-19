import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by viacheslav on 16.09.2015.
 */
public class Main {

    public static void main(String[] args) {

        if (args.length != 0 && args[0].equals("-h")) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                NetworkInterface network = NetworkInterface.getByInetAddress(ip);

                System.out.println("Available Network Interfaces:");
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets))
                    Server.displayInterfaceInformation(netint);

            } catch (UnknownHostException | SocketException e) {
                e.printStackTrace();
            }
        }

        Server server = new Server();
        server.run();
        Client client = new Client();
        client.run();
    }
}
