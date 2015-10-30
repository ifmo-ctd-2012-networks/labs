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

    public Node left() {
        if (nodes.size() == 1) {
            return null;
        }
        Map.Entry<String, Node> entry = nodes.lowerEntry(node.getMacAddress());
        if (entry != null) {
            entry.getValue();
        }
        entry = nodes.lastEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public Node right() {
        if (nodes.size() == 1) {
            return null;
        }
        Map.Entry<String, Node> entry = nodes.higherEntry(node.getMacAddress());
        if (entry != null) {
            return entry.getValue();
        }
        entry = nodes.firstEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public List<Node> neighbours() {
        if (nodes.size() == 1) {
            return Collections.emptyList();
        }
        List<Node> temp = new ArrayList<>();
        Node prev = left();
        if (prev != null && !prev.getMacAddress().equals(node.getMacAddress())) {
            temp.add(prev);
        }
        Node next = right();
        if (next != null && prev != next && !next.getMacAddress().equals(node.getMacAddress())) {
            temp.add(next);
        }
        return temp;
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
