import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Vector;

/**
 * Created by Георгий on 26.09.2015.
 */
public class Server extends Thread{
    private final DatagramSocket socket;
    private static final int PACKET_MAX_LENGTH = 500;
    private List<Packet> packets = new Vector<>();

    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }
    @Override
    public void run(){
        while(true){
            byte[] packetBuffer = new byte[PACKET_MAX_LENGTH];
            DatagramPacket datagramPacket = new DatagramPacket(packetBuffer, PACKET_MAX_LENGTH);
            try {
                socket.receive(datagramPacket);
                synchronized (this){
                    packets.add(Packet.getInstance(datagramPacket.getData()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized List<Packet> getPackets(){
        List<Packet> data = packets;
        packets = new Vector<>();
        return data;
    }
}
