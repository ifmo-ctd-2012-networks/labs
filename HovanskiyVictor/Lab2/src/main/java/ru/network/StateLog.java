package ru.network;

import java.io.*;
import java.net.InetAddress;

/**
 * @author victor
 */
public class StateLog {

    private final ServerNode node;

    public StateLog(ServerNode node) {
        this.node = node;
    }

    public void save() {
        try (PrintWriter writer = new PrintWriter(new File(node.getMacAddress() + ".dat"))) {
            node.getRing().all().forEach(n -> {
                writer.println(n.getHostname() + " " + n.getPort() + " " + n.getMacAddress() + " " + n.getAddress());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restore() {
        if (new File(node.getMacAddress() + ".dat").exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(node.getMacAddress() + ".dat"))) {
                while (reader.ready()) {
                    String[] line = reader.readLine().split(" ");
                    String hostname = line[0];
                    int port = Integer.parseInt(line[1]);
                    String macAddress = line[2];
                    InetAddress address = InetAddress.getByName(line[3].substring(1, line[3].length()));
                    Node n = node.getRing().findNodeByAddress(address);
                    n.setHostname(hostname);
                    n.setPort(port);
                    n.setMacAddress(macAddress);
                    node.getRing().put(n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
