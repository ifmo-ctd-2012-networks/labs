package src;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class Server implements Runnable {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public byte[] getMacAddress() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement().getHostAddress();
                }
            }
            InetAddress name = InetAddress.getByName(ip);
            NetworkInterface network = NetworkInterface.getByInetAddress(name);
            return network.getHardwareAddress();
        } catch (SocketException | UnknownHostException e) {
            Logger.commit("WTF", "Can't get MAC address");
        }
        return null;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try (DatagramSocket socket = new DatagramSocket()) {
                    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                            continue;
                        }

                        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                            InetAddress broadcast = interfaceAddress.getBroadcast();
                            if (broadcast == null) {
                                continue;
                            }

                            Message message = new Message(getHostName(), getMacAddress(), getTimestamp());
                            DatagramPacket packet = new DatagramPacket(message.toByteArray(), message.length(), broadcast, port);
                            socket.send(packet);
                            Logger.commit("Server:", String.format("Message: \'%s\' sent", message));
                        }
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000);
    }


    private int getTimestamp() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private byte[] getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

}