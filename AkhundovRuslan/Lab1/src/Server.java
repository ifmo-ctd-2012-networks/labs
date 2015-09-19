import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author korektur
 *         15/09/2015
 */
public class Server implements Runnable {
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int TIMEOUT = 5000;
    private final int port;
    private final byte[] bytes;

    public Server(int clientPort) throws SocketException, UnknownHostException {
        this.port = clientPort;
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

        byte[] hostnameBytes = hostname.getBytes(CHARSET);
        byte[] len = {(byte)hostnameBytes.length};
        bytes = mergeRequestParts(macAddress, mergeRequestParts(len, hostnameBytes));
    }

    private static byte[] mergeRequestParts(byte[] fst, byte[] snd) {
        byte[] merged = new byte[fst.length + snd.length];
        System.arraycopy(fst, 0, merged, 0, fst.length);
        System.arraycopy(snd, 0, merged, fst.length, snd.length);
        return merged;
    }


    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            while (!Thread.currentThread().isInterrupted()) {

                long startTime = System.currentTimeMillis();

                Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }

                        try {
                            int timestamp = (int)(System.currentTimeMillis() / 1000L);
                            byte[] timestampBytes = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(timestamp).array();

                            byte[] toSend = mergeRequestParts(bytes, timestampBytes);
                            DatagramPacket sendPacket = new DatagramPacket(toSend, toSend.length, broadcast, port);
                            socket.send(sendPacket);

                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Exception while trying to send packet", e);
                        }
                   }
                }

                if (TIMEOUT - (System.currentTimeMillis() - startTime) > 0) {
                    try {
                        Thread.sleep(TIMEOUT - (System.currentTimeMillis() - startTime));
                    } catch (InterruptedException e) {
                        LOG.log(Level.SEVERE, "Exception while trying to wait", e);
                    }
                }
            }
        } catch (SocketException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
