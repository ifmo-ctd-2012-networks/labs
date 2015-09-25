package com.lukin.network.lab1;

import javafx.util.Pair;

import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Саша on 19.09.2015.
 */
public class Main implements Runnable{
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final String hostname;
    private final int port;
    private final Map<MACAddress, Instance> map = new HashMap<>();

    public Main(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println("Wrong program usage. Required \"hostname port\"");
            return;
        }
        new Main(args[0], Integer.parseInt(args[1])).run();
    }

    @Override
    public void run() {
        Printer printer = new Printer(System.out);
        Client client = new Client(port);
        client.setMessageListener(data -> {
            printer.println("Thread: " + Thread.currentThread().getId() + " -> " + data);
            synchronized (map){
                Instance instance = map.get(data.getMacAddress());
                if (instance == null){
                    instance = new Instance(data.getMacAddress(), data.getHostname());
                    map.put(data.getMacAddress(), instance);
                }
                instance.setValid(true);
            }
        });
        Server server = new Server();
        server.setMessageListener(data -> {
            printer.println("Thread: " + Thread.currentThread().getId() + " -> " + data);
        });
        executorService.submit(printer);
        executorService.submit(client);
        executorService.submit(server);
        while (!Thread.interrupted()){
            try {
                Pair<byte[], InetAddress> pair = getBroadcast();
                Data data = new Data(new MACAddress(pair.getKey()), hostname, Instant.now().getEpochSecond());
                InetSocketAddress inetSocketAddress = new InetSocketAddress(pair.getValue(), port);
                server.send(inetSocketAddress, data);
            } catch (Exception e) {
                System.out.println("Sending failed");
            }
            synchronized (map){
                StringBuilder sb = new StringBuilder();
                sb.append("Connection: \n");
                map.values().stream().sorted((l, r) -> l.getMacAddress().compareTo(r.getMacAddress())).forEach(instance -> sb.append(instance).append("\n"));
                System.out.println("========================================================================");
                printer.println(sb.toString().equals("Connection: \n") ? "No connections" : sb);
                List<MACAddress> removed = new ArrayList<>(map.size());
                for (Instance instance : map.values()) {
                    if (instance.isValid()) {
                        instance.setValid(false);
                    } else {
                        instance.packetLost();
                    }
                    if (instance.getMissedPackets() > 5) {
                        removed.add(instance.getMacAddress());
                    }
                }
                removed.forEach(map::remove);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Pair<byte[], InetAddress> getBroadcast() throws SocketException {
        Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (interfaceEnumeration.hasMoreElements()){
            NetworkInterface next = interfaceEnumeration.nextElement();
            if (next.getHardwareAddress() != null){
                for (InterfaceAddress interfaceAddress : next.getInterfaceAddresses()){
                    InetAddress inetAddress = interfaceAddress.getBroadcast();
                    if (inetAddress != null){
                        return new Pair<>(next.getHardwareAddress(), inetAddress);
                    }
                }
            }
        }
        throw new SocketException("No broadcast available");
    }
}
