import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Георгий on 26.09.2015.
 */
public class Client extends Thread {
    public static final int SLEEP_TIME = 5000;
    private final int port;

    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            DatagramSocket socket = new DatagramSocket();
            while (true) {
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface network = interfaces.nextElement();
                        if (network.isUp() && !network.isLoopback()) {
                            List<InterfaceAddress> attachedAddresses = network.getInterfaceAddresses();
                            if (attachedAddresses != null) {
                                for (InterfaceAddress address : attachedAddresses) {
                                    if (address.getBroadcast() != null) {
                                        Packet packet = Packet.newInstance(network.getHardwareAddress(),
                                                hostName, System.currentTimeMillis() / 1000l);
                                        byte[] buffer = packet.getBytes();
                                        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length,
                                                address.getBroadcast(), port);
                                        socket.send(datagramPacket);
                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Hostname is unknown");
        } catch (SocketException e) {
            System.err.println("Can't create socket");
        }
    }
}
