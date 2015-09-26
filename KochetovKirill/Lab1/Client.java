
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class Client extends Thread {
    private final int port;
    Message messageToSend;
    InetAddress addrs;
    private final DatagramSocket socket = new DatagramSocket();

    public Client(int port) throws SocketException, UnknownHostException {
        this.port = port;
        NetworkInterface nw = NetworkInterface.getByName("wlan0");
        messageToSend = new Message(nw.getHardwareAddress(), InetAddress.getLocalHost().getHostName());
        addrs = nw.getInterfaceAddresses().get(0).getBroadcast();
    }

    @Override
    public void run() {
        while (true) {
                long time = System.currentTimeMillis();
                messageToSend.timeStamp = time / 1000L;
                try {
                    byte[] messageBytes = messageToSend.getBytes();
                    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, addrs, port);
                    socket.send(packet);
                    System.out.println("send");
                } catch (UnsupportedEncodingException e) {
                        System.err.println("Unsupported encoding: " + e.getMessage());
                } catch (UnknownHostException e) {
                        System.err.println("Unknown host: " + e.getMessage());
                } catch (IOException e) {
                        System.err.println("Error while sending message: " + e.getMessage());
                }
                try {
                    Thread.sleep(4500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    public void close() {
        socket.close();
        interrupt();
    }
}
