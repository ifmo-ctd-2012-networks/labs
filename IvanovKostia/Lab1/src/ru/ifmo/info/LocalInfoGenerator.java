package ru.ifmo.info;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;

public class LocalInfoGenerator implements Iterator<NodeInfo> {
    private final Logger logger = Logger.getLogger(LocalInfoGenerator.class);

    private final Enumeration<NetworkInterface> interfaces;
    private NodeInfo nextNode;

    public LocalInfoGenerator() {
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException("Failed to get network interfaces");
        }
    }

    /**
     * Places next available node info (if exists) at <tt>nextNode</tt> (if it's free)
     */
    private void prepareNext() {
        if (nextNode != null) return;

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            try {
                byte[] hardwareAddress = networkInterface.getHardwareAddress();

                if (hardwareAddress != null) {
                    MacAddress mac = new MacAddress(hardwareAddress);

                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    if (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();

                        String hostName = inetAddress.getHostName();
                        nextNode = new NodeInfo(mac, hostName);
                        return;
                    }
                }
            } catch (SocketException e) {
                logger.warn("Failed to get hardware address of network interface " + networkInterface);
            }
        }
    }


    @Override
    public NodeInfo next() {
        prepareNext();
        try {
            return nextNode;
        } finally {
            nextNode = null;
        }
    }

    @Override
    public boolean hasNext() {
        prepareNext();
        return nextNode != null;
    }
}
