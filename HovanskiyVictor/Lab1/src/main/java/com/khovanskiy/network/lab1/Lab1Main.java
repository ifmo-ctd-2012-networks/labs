package com.khovanskiy.network.lab1;

import com.khovanskiy.network.lab1.model.MacAddress;
import com.khovanskiy.network.lab1.model.Message;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Полезная статья http://xgu.ru/wiki/%D0%A1%D0%B5%D1%82%D0%B5%D0%B2%D0%BE%D0%B9_%D0%B8%D0%BD%D1%82%D0%B5%D1%80%D1%84%D0%B5%D0%B9%D1%81
 * @author victor
 */
public class Lab1Main implements Runnable {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: hostname port");
        }
        new Lab1Main(args[0], Integer.parseInt(args[1])).run();
    }

    private final ExecutorService service = Executors.newFixedThreadPool(3);
    private final String hostname;
    private final int port;

    public Lab1Main(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("Main thread: " + Thread.currentThread().getId());
        Printer printer = new Printer(System.out);
        Receiver receiver = new Receiver(port);
        receiver.setOnSuccessListener(message -> {
            System.out.println("Thread #" + Thread.currentThread().getId() + " | <- " + message);
        });
        Sender sender = new Sender();
        sender.setOnSuccessListener(message -> {
            System.out.println("Thread #" + Thread.currentThread().getId() + " | -> " + message);
        });
        //service.submit(printer);
        service.submit(receiver);
        service.submit(sender);

        while (true) {
            try {
                Pair<byte[], InetAddress> pair = getBroadcast();
                MacAddress macAddress = new MacAddress(pair.getKey());
                InetAddress address = pair.getValue();
                long timestamp = getUnixTimestamp();
                Message message = new Message(macAddress, hostname, timestamp);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
                sender.send(inetSocketAddress, message);
            } catch (IOException e) {

            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*InetAddress ip;
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
                System.out.println(sb.toString());
            }
            NetworkInterface network = NetworkInterface.get

            byte[] mac = network.getHardwareAddress();

            System.out.print("Current MAC address : ");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private Pair<byte[], InetAddress> getBroadcast() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            //System.out.println(networkInterface + " " + new MacAddress(networkInterface.getHardwareAddress()));
            if (networkInterface.getHardwareAddress() != null) {
                for (InterfaceAddress interfaceAddess : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddess.getBroadcast();
                    if (address != null) {
                        return new Pair<>(networkInterface.getHardwareAddress(), address);
                    }
                }
            }
        }
        throw new SocketException("No broadcast address found");
    }

    public long getUnixTimestamp() {
        return Instant.now().getEpochSecond();
    }


}
