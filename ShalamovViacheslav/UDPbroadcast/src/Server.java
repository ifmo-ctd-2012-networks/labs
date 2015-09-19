import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by viacheslav on 18.09.2015.
 * <p>
 * Announce-server. Every 5 sec sends Announce-packets
 * to all broadcast addresses of all network interfaces.
 */
public class Server implements Runnable {

    private static byte hostnameLength;
    private static String hostname;

    public static final int DELAY = 5000;
    public static final int PORT = 8888;

    public Server() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            hostnameLength = (byte) ByteUtls.stringToBytes(hostname).length;

            System.out.println(hostnameLength);

            System.out.println("running instance: " + " " + hostname);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Displays information about network interface
     *
     * @param netint network interface to be examined
     * @throws SocketException
     */
    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.format("Display name: %s\n", netint.getDisplayName());
        System.out.format("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.format("InetAddress: %s\n", inetAddress);
        }
        System.out.println();
    }

    /**
     * Every 5 sec sends Announce-packets
     * to all broadcast addresses of all network interfaces.
     */
    @Override
    public void run() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {

                    Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                            .filter(networkInterface -> {
                                try {
                                    return networkInterface.isUp() && (!networkInterface.isLoopback());
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .forEach(network -> {
                                try {
                                    final byte[] mac = network.getHardwareAddress();
                                    if (mac != null)
                                        network.getInterfaceAddresses().stream()
                                                .filter(address -> address.getBroadcast() != null)
                                                .forEach(address -> {
                                                    InetAddress broadcastAddress = address.getBroadcast();
                                                    sendPacket(mac, broadcastAddress);
                                                });
                                } catch (SocketException e) {
                                    System.err.println(e.getMessage());
                                }
                            });
                } catch (SocketException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 0, DELAY);
    }

    /**
     * Constructs and sends packet with specified mac to the specifier address.
     *
     * @param mac
     * @param address
     */
    private void sendPacket(byte[] mac, InetAddress address) {
        byte[] buf = new byte[1000];
        int macLength = mac.length;
        System.arraycopy(mac, 0, buf, 0, macLength);
        buf[macLength] = hostnameLength;
        System.arraycopy(ByteUtls.stringToBytes(hostname), 0, buf, macLength + 1, (int) hostnameLength);

        byte[] unixTimeBytes = ByteUtls.unixTimeToBytes(System.currentTimeMillis() / 1000);

        System.arraycopy(unixTimeBytes, 0, buf, macLength + 1 + hostnameLength, unixTimeBytes.length);

        System.err.println(getClass().getName() + ">>>Packet ready: "
                + ByteUtls.bytesToHex(mac) + " "
                + ByteUtls.bytesToHex(new byte[]{hostnameLength}) + " "
                + ByteUtls.bytesToHex(ByteUtls.stringToBytes(hostname))
                + " " + ByteUtls.bytesToHex(unixTimeBytes));


        try {
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            packet.setPort(PORT);
            packet.setAddress(address);
            c.send(packet);
            System.err.println(getClass().getName() + ">>> Packet sent to: " + ByteUtls.bytesToDec(address.getAddress()) + ":" + PORT);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

