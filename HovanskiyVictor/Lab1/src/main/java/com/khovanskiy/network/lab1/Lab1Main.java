package com.khovanskiy.network.lab1;

import com.khovanskiy.network.lab1.model.Instance;
import com.khovanskiy.network.lab1.model.MacAddress;
import com.khovanskiy.network.lab1.model.Message;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Полезная статья http://xgu.ru/wiki/%D0%A1%D0%B5%D1%82%D0%B5%D0%B2%D0%BE%D0%B9_%D0%B8%D0%BD%D1%82%D0%B5%D1%80%D1%84%D0%B5%D0%B9%D1%81
 *
 * @author victor
 */
public class Lab1Main implements Runnable {

    private final static int MAX_DELAY = 3000;
    private final static int MAX_MISSED_PACKETS = 5;
    private final ExecutorService service = Executors.newFixedThreadPool(3);
    private final String hostname;
    private final int port;
    private final Map<MacAddress, Instance> instanceMap = new HashMap<>();

    public Lab1Main(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: hostname port");
        }
        new Lab1Main(args[0], Integer.parseInt(args[1])).run();
    }

    @Override
    public void run() {
        Printer printer = new Printer(System.out);
        printer.println("Host \"" + hostname + "\" started at " + port);
        printer.println("Main thread: " + Thread.currentThread().getId());
        Receiver receiver = new Receiver(port);
        receiver.setOnSuccessListener(message -> {
            printer.println("Thread #" + Thread.currentThread().getId() + " | <- " + message);
            synchronized (instanceMap) {
                Instance instance = instanceMap.get(message.getMacAddress());
                if (instance == null) {
                    instance = new Instance(message.getMacAddress(), message.getHostname());
                    instanceMap.put(message.getMacAddress(), instance);
                }
                instance.setActual(true);
            }
        });
        Sender sender = new Sender();
        sender.setOnSuccessListener(message -> {
            printer.println("Thread #" + Thread.currentThread().getId() + " | -> " + message);
        });
        service.submit(printer);
        service.submit(receiver);
        service.submit(sender);

        while (!Thread.interrupted()) {
            try {
                Pair<byte[], InetAddress> pair = getBroadcast();
                MacAddress macAddress = new MacAddress(pair.getKey());
                InetAddress address = pair.getValue();
                long timestamp = getUnixTimestamp();
                Message message = new Message(macAddress, hostname, timestamp);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
                sender.send(inetSocketAddress, message);
            } catch (IOException ignored) {
            }
            synchronized (instanceMap) {
                StringBuilder sb = new StringBuilder();
                sb.append("Current status:\n");
                instanceMap.values().stream().sorted((l, r) -> l.getMacAddress().compareTo(r.getMacAddress())).forEach(instance -> sb.append(instance).append("\n"));
                printer.println(sb.toString());

                List<MacAddress> removed = new ArrayList<>(instanceMap.size());
                for (Instance instance : instanceMap.values()) {
                    if (instance.isActual()) {
                        instance.setActual(false);
                    } else {
                        instance.missPacket();
                    }
                    if (instance.getMissedPackets() > MAX_MISSED_PACKETS) {
                        removed.add(instance.getMacAddress());
                    }
                }
                removed.forEach(instanceMap::remove);
            }
            try {
                Thread.sleep(MAX_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Pair<byte[], InetAddress> getBroadcast() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
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
