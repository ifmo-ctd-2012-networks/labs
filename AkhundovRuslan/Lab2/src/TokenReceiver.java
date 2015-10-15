import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author korektur
 *         15/10/2015
 */
public class TokenReceiver implements Runnable {

    private static final Logger LOG = Logger.getLogger(TokenReceiver.class.getName());

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            try (Socket socket = new Socket("localhost", Settings.TOKEN_RECEIVER_PORT)) {

                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                Token token = (Token) objectInputStream.readObject();

                socket.close();

            } catch (IOException | ClassNotFoundException e) {
                LOG.log(Level.SEVERE, e.getClass() + ": " + e.getMessage());
            }
        }
    }
}
