package ru.network;

import java.net.InetAddress;
import java.util.*;

/**
 * @author victor
 */
public class Ring {

    private final NavigableMap<String, Node> nodes = new TreeMap<>();
    private final ServerNode node;

    public Ring(ServerNode node) {
        this.node = node;
    }

    public void put(Node node) {
        nodes.put(node.getMacAddress(), node);
    }

    public void clear() {
        nodes.clear();
    }

    public List<Node> all() {
        return new ArrayList<>(nodes.values());
    }

    public List<Node> neighbours() {
        if (nodes.size() == 1) {
            return Collections.emptyList();
        }
        Node[] array = new Node[nodes.values().size()];
        array = nodes.values().toArray(array);
        int prev = -1;
        int next = -1;
        int cur = -1;
        for (int i = 0; i < array.length; ++i) {
            Node current = array[i];
            if (current.getMacAddress().equals(node.getMacAddress())) {
                prev = (array.length + i - 1) % array.length;
                cur = i;
                next = (i + 1) % array.length;
            }
        }
        List<Node> list = new ArrayList<>();
        if (prev != cur) {
            list.add(array[prev]);
        }
        if (next != prev && next != cur) {
            list.add(array[next]);
        }
        return list;
    }

    public Node findNodeByAddress(InetAddress address) {
        Optional<Node> t = nodes.values().stream().filter(n -> n.getAddress().equals(address)).findFirst();
        if (t.isPresent()) {
            return t.get();
        }
        Node node = new Node();
        node.setAddress(address);
        return node;
    }

    @Override
    public String toString() {
        return "Ring" + nodes.values();
    }
}
