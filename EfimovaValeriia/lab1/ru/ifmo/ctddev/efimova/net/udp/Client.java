package ru.ifmo.ctddev.efimova.net.udp;

import static ru.ifmo.ctddev.efimova.net.udp.Constants.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class Client implements Runnable {

    public byte[] IP_ADDR;
    public byte[] MAC_ADDR;
    public byte[] HOSTNAME;
    public byte HN_LEN;

    public void init() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        getIP: while (interfaces.hasMoreElements()) {
            NetworkInterface element = interfaces.nextElement();
            if (element.isLoopback() || !element.isUp()) {
                continue;
            }

            Enumeration<InetAddress> addresses = element.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress i = addresses.nextElement();
                if (i instanceof Inet4Address) {
                    ip = i;
                    System.out.println("Current IP address : " + ip.getHostAddress());
                    break getIP;
                }
            }
        }

        String[] ab = ip.toString().substring(1).split("\\.");
        byte[] ipb = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipb[i] = Integer.valueOf(ab[i]).byteValue();
        }

        IP_ADDR = ipb;
        MAC_ADDR = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
        String name = "Valery";
        HOSTNAME = name.getBytes(Charset.forName("UTF-8"));
        HN_LEN = (byte)HOSTNAME.length;

        printMAC();
    }

    private void printMAC() {
        System.out.print("Current MAC address : ");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAC_ADDR.length; i++) {
            sb.append(String.format("%02X%s", MAC_ADDR[i], (i < MAC_ADDR.length - 1) ? "-" : ""));
        }
        System.out.println(sb.toString());
    }

    public class ClientTask extends TimerTask {
        private byte[] data;

        public ClientTask() {
            data = new byte[MAC_LEN + HOSTNAME.length + 1 + T_LEN];
            System.arraycopy(MAC_ADDR, 0, data, 0, MAC_LEN);
            data[MAC_LEN] = HN_LEN;
            System.arraycopy(HOSTNAME, 0, data, MAC_LEN + 1, HOSTNAME.length);
        }

        @Override
        public void run() {
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                clientSocket.setBroadcast(true);
                InetAddress IPAddress = InetAddress.getByName(BROADCAST_ADDRESS);

                int timestamp = (int) (System.currentTimeMillis() / 1000);
                byte[] timestampBytes = ByteBuffer.allocate(T_LEN).putInt(timestamp).array();
                System.arraycopy(timestampBytes, 0, data, MAC_LEN + HN_LEN + 1, T_LEN);

                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, PORT);
                clientSocket.send(sendPacket);
                clientSocket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            init();
            Timer t = new Timer();
            t.scheduleAtFixedRate(new ClientTask(), 0, PERIOD);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}


