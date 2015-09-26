package ru.ifmo.broadcast;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

/**
 * Prints networks at which broadcast is available
 */
public class AvailableNetworks {
    public static void main(String[] args) throws SocketException {
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            networkInterface.getInterfaceAddresses().stream()
                    .filter(adresses -> adresses.getBroadcast() != null)
                    .forEach(adresses -> System.out.println(networkInterface + " at " + adresses.getBroadcast()));
        }

    }
}
