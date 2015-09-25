import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by viacheslav on 18.09.2015.
 */
public class Client implements Runnable {


    public static final int PORT = 8888;

    public static final int DELAY = 5000;
    public static final String LOCAL_ADDRESS = "0.0.0.0";

    /**
     * Currently alive instances.
     */
    Map<String, NodeInfo> instances;

    /**
     * received packets to be processes at next iteration.
     */
    ConcurrentLinkedDeque<DatagramPacket> packets;
    private int iteration;

    public Client() {
        instances = new HashMap<>();
        packets = new ConcurrentLinkedDeque<>();
        iteration = 0;
    }

    @Override
    public void run() {
        listen();
        update();
    }


    /**
     * Listens to packets and puts them to the queue.
     */
    public void listen() {
        new Thread() {
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getByName(LOCAL_ADDRESS));
                    socket.setBroadcast(true);

                    System.err.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
                    while (true) {

                        byte[] recvBuf = new byte[15000];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);

                        System.err.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());

                        packets.addLast(packet);
                    }
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }.start();
    }


    /**
     * Updates information about alive instances in the network every 5 sec.
     */
    public void update() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ++iteration;
                DatagramPacket packet;
                while ((packet = packets.poll()) != null) {
                    try {
                        byte[] recvBuf = packet.getData();
                        byte[] recvMac = new byte[6];
                        System.arraycopy(recvBuf, 0, recvMac, 0, 6);
                        byte hostLen = recvBuf[6];
                        byte[] recvHost = new byte[hostLen];
                        System.arraycopy(recvBuf, 7, recvHost, 0, (int) hostLen);
                        NodeInfo info = new NodeInfo(recvMac, new String(recvHost, "UTF-8"), iteration);
                        instances.put(ByteUtls.bytesToHex(recvMac), info);
                    } catch (ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                List<NodeInfo> sorted = new ArrayList<>();
                List<String> toBeRemoved = new ArrayList<>();
                for (Map.Entry<String, NodeInfo> entry : instances.entrySet()) {
                    if (iteration - entry.getValue().getLastIteration() >= 5)
                        toBeRemoved.add(entry.getKey());
                    else
                        sorted.add(entry.getValue());

                }
                for (String s : toBeRemoved)
                    instances.remove(s);

                Collections.sort(sorted);
                for (NodeInfo n : sorted) {
                    System.out.println(n.getHostname() + ":");
                    System.out.println("    " + ByteUtls.bytesToHex(n.getMac())
                            + " -- packets lost:" + (iteration - n.getLastIteration()));
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, DELAY, DELAY);
    }

}
