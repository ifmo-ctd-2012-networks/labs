import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author korektur
 *         15/09/2015
 */
public class Server implements Runnable {
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    private static final int SO_TIMEOUT = 5000;

    private final int port;
    private final String pack;
    private DatagramSocket socket;

    public Server(int port) throws SocketException, UnknownHostException {
        this.port = port;
        InetAddress ip = InetAddress.getLocalHost();

        System.out.println("Current ip address : " + ip.getHostAddress());

        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        byte[] macAddress = network.getHardwareAddress();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macAddress.length; i++) {
            sb.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
        }

        System.out.println(sb.toString());

        String hostname = InetAddress.getLocalHost().getHostName();

        pack = sb.append(hostname.length()).append(hostname).toString();
    }

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            socket.setSoTimeout(SO_TIMEOUT);

            //noinspection InfiniteLoopStatement
            while (true) {
                System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");

                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Packet received
                System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

                long timestamp = System.currentTimeMillis();

                byte[] toSend = (pack + timestamp).getBytes(StandardCharsets.UTF_8);

                //Send a response
                DatagramPacket sendPacket = new DatagramPacket(toSend, toSend.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);

                System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());

            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
