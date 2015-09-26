package ru.zyulyaev.ifmo.net.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zyulyaev.ifmo.net.lab1.components.BackgroundComponent;
import ru.zyulyaev.ifmo.net.lab1.components.ComponentInitializationException;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zyulyaev
 */
public class UdpAttacker extends BackgroundComponent {
    private static final Logger log = LoggerFactory.getLogger(UdpAttacker.class);

    private final int port;

    public UdpAttacker(int port) {
        this.port = port;
    }

    @Override
    protected void init() throws ComponentInitializationException {
    }

    private static byte[] randomData() {
        int length = ThreadLocalRandom.current().nextInt(1000);
        byte[] data = new byte[length];
        ThreadLocalRandom.current().nextBytes(data);
        return data;
    }

    @Override
    protected void doInBackground() {
        while (!Thread.interrupted()) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                byte[] data = randomData();

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null)
                        continue;
                    DatagramPacket packet = new DatagramPacket(data, data.length, new InetSocketAddress(broadcast, port));
                    socket.send(packet);
                }
            } catch (IOException e) {
                log.info("Attack failed :(", e);
            }
        }
    }

    @Override
    protected void cleanup() {

    }
}
