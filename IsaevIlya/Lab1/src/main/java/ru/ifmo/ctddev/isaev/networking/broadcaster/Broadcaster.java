package ru.ifmo.ctddev.isaev.networking.broadcaster;

import java.io.IOException;
import java.net.*;

import static ru.ifmo.ctddev.isaev.networking.Main.SLEEP_TIME;

/**
 * @author Ilya Isaev
 */
public abstract class Broadcaster implements Runnable {

    public static InetAddress getBroadcastAddress(NetworkInterface network) throws SocketException {
        for (InterfaceAddress interfaceAddress :
                network.getInterfaceAddresses()) {
            InetAddress broadcast = interfaceAddress.getBroadcast();
            if (broadcast != null) {
                return broadcast;
            }
        }
        return null;
    }


    public abstract DatagramPacket getBroadcastPacket() throws SocketException, UnknownHostException;

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            while (true) {
                try {
                    socket.send(getBroadcastPacket());
                    Thread.sleep(SLEEP_TIME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | SocketException e) {
            e.printStackTrace();
        }
    }


}
