import com.sun.org.apache.bcel.internal.generic.NEW;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class Server implements Runnable {

    public static final int SLEEP_TIMEOUT = 5000;

    private final int port;
    private final String hostName;

    private NetworkInterface networkInterface;
    private InetAddress broadcastAddress;

    public Server(int port) throws UnknownHostException, SocketException {
        this.port = port;
        this.hostName = InetAddress.getLocalHost().getHostName();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface nI = interfaces.nextElement();
            if (nI.isUp() && !nI.isLoopback()) {
                List<InterfaceAddress> attachedAddresses = nI.getInterfaceAddresses();
                if (attachedAddresses != null) {
                    for (InterfaceAddress address : attachedAddresses) {
                        if (address.getBroadcast() != null) {
                            this.networkInterface = nI;
                            this.broadcastAddress = address.getBroadcast();
                        }
                    }
                }
            }
        }

    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    Packet packet = Packet.newInstance(networkInterface.getHardwareAddress(),
                            hostName, System.currentTimeMillis());
                    byte[] buffer = packet.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length,
                            broadcastAddress, port);
                    socket.send(datagramPacket);
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
