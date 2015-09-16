import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author korektur
 *         15/09/2015
 */
public class Client implements Runnable {
    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    private static final int SO_TIMEOUT = 5000;

    private final int PORT;
    private final int TO_PORT;

    public Client(int port, int toPort) {
        PORT = port;
        TO_PORT = toPort;
    }


    @Override
    public void run() {
        DatagramSocket c;
        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            c = new DatagramSocket();
            c.setBroadcast(true);
            c.setSoTimeout(SO_TIMEOUT);
            while (true) {

                byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

                //Try the 255.255.255.255 first
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), PORT);
                    c.send(sendPacket);
                    System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
                } catch (Exception e) {
                }
                // Broadcast the message over all the network interfaces
                Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }

                        // Send the broadcast package!
                        try {
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, TO_PORT);
                            c.send(sendPacket);
                        } catch (Exception e) {
                        }

                        System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    }
                }

                System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");

                //Wait for a response
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                c.receive(receivePacket);

                //We have a response
                System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();
                System.out.println(PORT + ": " + message);
            }
            //Close the port!
//            c.close();

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
