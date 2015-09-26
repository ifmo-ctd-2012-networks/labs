package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.concurrent.*;

/**
 * Created by kerzo on 19.09.2015.
 */
public class UDPServer implements Runnable {

    private ConcurrentSkipListSet<Node> incomingMessages;
    private ConcurrentHashMap<String, Node> hostByMac;
    private int port;

    public UDPServer(int port) {
        this.incomingMessages = new ConcurrentSkipListSet<>();
        this.hostByMac = new ConcurrentHashMap<>();
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            Timer t = new Timer();
            t.scheduleAtFixedRate(new MessageHandler(incomingMessages), 0, 5000);
            while (!Thread.interrupted()) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivedPacket);
                    Message receivedMessage = new Message(buffer);
                    long curTime = System.currentTimeMillis();
                    Node storedHost = hostByMac.get(receivedMessage.getMacAddress());
                    if (storedHost == null) {
                        storedHost = new Node(receivedMessage, curTime);
                        hostByMac.put(storedHost.getMacAddress(), storedHost);
                    }
                    /*else {
                        hostByMac.get(receivedMessage.getMacAddress()).update(curTime, receivedMessage.getTimestamp(), true);
                    }*/

                    for (Node storeHost : hostByMac.values()) {
                        if (storeHost.equals(storedHost))
                            storeHost.update(curTime, storedHost.getTimestamp(), true);
                        else
                            storeHost.update(curTime, null, false);

                        if (curTime - storeHost.getLastRequest() >= 25001 || storeHost.getMissedRequestCount() > 5) {
                            hostByMac.remove(storeHost.getMacAddress());
                        }
                    }

                    incomingMessages.clear();
                    incomingMessages.addAll(hostByMac.values());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
