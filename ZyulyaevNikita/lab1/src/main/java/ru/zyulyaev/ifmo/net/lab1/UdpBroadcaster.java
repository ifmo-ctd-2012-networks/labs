package ru.zyulyaev.ifmo.net.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zyulyaev.ifmo.net.lab1.components.BackgroundComponent;
import ru.zyulyaev.ifmo.net.lab1.components.ComponentInitializationException;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

import static ru.zyulyaev.ifmo.net.lab1.utils.CloseableUtils.tryClose;

/**
 * @author zyulyaev
 */
public class UdpBroadcaster extends BackgroundComponent {
    private static final Logger log = LoggerFactory.getLogger(UdpBroadcaster.class);

    private final int port;
    private DatagramSocket socket;

    public UdpBroadcaster(int port) {
        this.port = port;
    }

    @Override
    protected void init() throws ComponentInitializationException {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (IOException e) {
            if (socket != null)
                tryClose(socket, "Failed to close socket");
            throw new ComponentInitializationException(e);
        }
    }

    @Override
    protected void doInBackground() {
        try {
            while (!Thread.interrupted()) {
                try {
                    InetAddress localHost = InetAddress.getLocalHost();
                    NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
                    byte[] mac = networkInterface.getHardwareAddress();
                    String hostName = localHost.getHostName();
                    UdpMessage message = new UdpMessage(mac, hostName, (int) (System.currentTimeMillis() / 1000));

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null)
                            continue;
                        DatagramPacket packet = message.toPacket();
                        packet.setSocketAddress(new InetSocketAddress(broadcast, port));
                        socket.send(packet);
                    }

                    TimeUnit.SECONDS.sleep(5);
                } catch (IOException e) {
                    log.error("Failed to broadcast packet", e);
                }
            }
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
        }
    }

    @Override
    protected void cleanup() {
        tryClose(socket, "Failed to close socket");
    }
}
