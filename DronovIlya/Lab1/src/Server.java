import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class Server implements Runnable {

    public static final int SLEEP_TIMEOUT = 5000;

    private final int port;
    private final String hostName;

    public Server(int port) throws UnknownHostException {
        this.port = port;
        this.hostName = InetAddress.getLocalHost().getHostName();
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = interfaces.nextElement();
                        if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                            List<InterfaceAddress> attachedAddresses = networkInterface.getInterfaceAddresses();
                            if (attachedAddresses != null) {
                                for (InterfaceAddress address : attachedAddresses) {
                                    if (address.getBroadcast() != null) {
                                        Packet packet = Packet.newInstance(networkInterface.getHardwareAddress(),
                                                hostName, System.currentTimeMillis());
                                        byte[] buffer = packet.getBytes();
                                        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length,
                                                address.getBroadcast(), port);
                                        socket.send(datagramPacket);
                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(SLEEP_TIMEOUT);
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
