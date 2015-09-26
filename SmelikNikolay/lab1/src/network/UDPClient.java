package network;

import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kerzo on 19.09.2015.
 */
public class UDPClient implements Runnable {
    private final byte[] hostNameBytes;
    private final byte hostNameLength;
    private byte[] timestampBytes;
    private final byte[] macBytes;
    private int port;

    public UDPClient(int port) throws IOException {
        this.port = port;
        String hostName = InetAddress.getLocalHost().getHostName();
        macBytes = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
        hostNameBytes = hostName.getBytes();
        hostNameLength = (byte)hostNameBytes.length;
        timestampBytes = Utils.getTimestamp();
    }

    public class ClientTask extends TimerTask {
        private int port;

        public ClientTask(int port) {
            this.port = port;

        }

        private byte[] CreateSendData() {
            byte[] sendData = new byte[Utils.HOSTNAME_OFFSET + hostNameLength + timestampBytes.length];
            timestampBytes = Utils.getTimestamp();
            System.arraycopy(macBytes, 0, sendData, 0, macBytes.length);
            sendData[Utils.HOSTNAME_OFFSET - 1] = hostNameLength;
            System.arraycopy(hostNameBytes, 0, sendData, Utils.HOSTNAME_OFFSET, hostNameBytes.length);
            System.arraycopy(timestampBytes, 0, sendData, Utils.HOSTNAME_OFFSET + hostNameLength, timestampBytes.length);
            return sendData;
        }

        @Override
        public void run() {
            try (MulticastSocket clientSocket = new MulticastSocket()) {
                clientSocket.setBroadcast(true);
                InetAddress IPAddress = InetAddress.getByName("255.255.255.255");
                byte[] sendData = CreateSendData();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                clientSocket.send(sendPacket);
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new ClientTask(port), 0, 5000);
    }
}
