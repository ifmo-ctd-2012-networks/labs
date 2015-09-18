package lab1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final DatagramSocket socket;
    private List<Message> messages = new ArrayList<>();
    private volatile boolean stopped;

    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        while (!stopped) {
            DatagramPacket packet = new DatagramPacket(new byte[Long.BYTES + 263], Long.BYTES + 263);
            try {
                socket.receive(packet);
                synchronized (this) {
                    messages.add(new Message(packet.getData()));
                }
            } catch (IOException e) {
                if (!stopped)
                    System.err.println("Error while receiving message: " + e.getMessage());
            }
        }
    }

    public synchronized List<Message> getMessages() {
        List<Message> result = messages;
        messages = new ArrayList<>();
        return result;
    }

    public void close() {
        stopped = true;
        socket.close();
    }
}
