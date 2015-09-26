
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final DatagramSocket socket;
    private List<Message> messages = new ArrayList<>();

    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);
                synchronized (messages) {
                    messages.add(new Message(packet.getData()));
                }
            } catch (IOException e) {
                System.err.println("Error while receiving message: " + e.getMessage());
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }
    }

    public List<Message> getMessages() {
        synchronized (messages) {
            List<Message> result = messages;
            messages = new ArrayList<>();
            return result;
        }
    }

    public void close() {
        socket.close();
        interrupt();
    }
}
