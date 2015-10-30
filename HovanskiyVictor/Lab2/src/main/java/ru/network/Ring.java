package ru.network;

import java.net.InetAddress;
import java.util.*;

/**
 * @author victor
 */
public class Ring {

    private final Map<String, Node> nodeMap = new HashMap<>();

    public Ring() {
    }

    public static Ring empty() {
        return new Ring();
    }

    public void put(Node node) {
        nodeMap.put(node.getMacAddress(), node);
    }

    public int getPositionOf(Node node) {
        return 0;
    }

    public List<Node> neighbours() {
        return Collections.emptyList();
    }

    public Node findNodeByAddress(InetAddress address) {
        return null;
    }
}
