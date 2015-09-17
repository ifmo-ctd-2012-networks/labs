package com.khovanskiy.network.lab1;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.util.Enumeration;

/**
 * Полезная статья http://xgu.ru/wiki/%D0%A1%D0%B5%D1%82%D0%B5%D0%B2%D0%BE%D0%B9_%D0%B8%D0%BD%D1%82%D0%B5%D1%80%D1%84%D0%B5%D0%B9%D1%81
 * @author victor
 */
public class Lab1Main implements Runnable {

    public static void main(String[] args) {
        new Lab1Main().run();
    }

    @Override
    public void run() {
        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                System.out.println(ni.getDisplayName() + " " + ni.getMTU() + " " + ni.isPointToPoint());
                ni.getInterfaceAddresses().forEach(interfaceAddress -> {
                    System.out.println(interfaceAddress + " " + interfaceAddress.getBroadcast() + " " + interfaceAddress.getNetworkPrefixLength());
                });
                /*Enumeration<NetworkInterface> sub = ni.getSubInterfaces();
                while (sub.hasMoreElements()) {
                    System.out.println("\t" + sub.nextElement());
                }*/
                /*byte[] mac = networkInterface.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                System.out.println(sb.toString());*/
            }
            /*NetworkInterface network = NetworkInterface.get

            byte[] mac = network.getHardwareAddress();

            System.out.print("Current MAC address : ");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getUnixTimestamp() {
        return Instant.now().getEpochSecond();
    }

    private class Server {

    }

    private class Client {

    }
}
