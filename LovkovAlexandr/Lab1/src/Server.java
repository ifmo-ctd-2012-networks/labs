import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;

/**
 * Created by Charm on 15/09/15.
 */
public class Server implements Runnable {

    Server(int port, int iterations) {
        this.port = port;
        this.iterations = iterations;
        System.out.println("Server, port:" + port);
    }

    private final int port;
    private final int iterations;

    @Override
    public void run() {
        int count = 0;
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] macAddress = MacAddress.getMacAdress();
            if (macAddress == null) {
                System.out.println("macAddress=null");
                return;
            }

            while (!Thread.currentThread().isInterrupted()) {
                count++;
                if (iterations != 0 && count > iterations) {
                    break;
                }
                //mac 6 bytes
                //hostname length 1 byte
                //hostname UTF-8 string
                //UNIX timestamp
                byte[] host = InetAddress.getLocalHost().getHostName().getBytes("UTF-8");
                byte[] length = new byte[1];
                length[0] = (byte) host.length;
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                long currentTime = System.currentTimeMillis();
                buffer.putLong(currentTime);
                byte[] timestamp = buffer.array();
                byte[] a1 = mergeArrays(macAddress, length);
                byte[] a2 = mergeArrays(a1, host);
                byte[] sendData = mergeArrays(a2, timestamp);

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
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
                            socket.send(sendPacket);
                        } catch (IOException e) {
                            System.out.println("can't send broadcast");
                        }
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e + " " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server can't create Datagram socket");
        }
    }

    private byte[] mergeArrays(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }
}
