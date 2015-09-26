package ru.zyulyaev.ifmo.net.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zyulyaev.ifmo.net.lab1.components.BackgroundComponent;
import ru.zyulyaev.ifmo.net.lab1.components.ComponentInitializationException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static ru.zyulyaev.ifmo.net.lab1.utils.CloseableUtils.tryClose;

/**
 * @author zyulyaev
 */
public class UdpServer extends BackgroundComponent {
    private static final Logger log = LoggerFactory.getLogger(UdpServer.class);
    private static final int BUFFER_SIZE = 1500;

    private final DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
    private final int port;
    private final UdpStatistics statistics;
    private DatagramSocket socket;

    public UdpServer(int port, UdpStatistics statistics) {
        this.port = port;
        this.statistics = statistics;
    }

    @Override
    protected void init() throws ComponentInitializationException {
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
        } catch (IOException e) {
            throw new ComponentInitializationException(e);
        }
    }

    @Override
    protected void doInBackground() {
        while (!Thread.interrupted()) {
            try {
                socket.receive(packet);
                statistics.processMessage(packet);
            } catch (IOException e) {
                log.error("Failed to receive UDP packet", e);
            }
        }
    }

    @Override
    protected void cleanup() {
        tryClose(socket, "Failed to close UDP channel");
    }
}
