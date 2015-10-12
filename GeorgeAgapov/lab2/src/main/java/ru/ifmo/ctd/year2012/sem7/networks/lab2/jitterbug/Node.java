package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import java.net.InetAddress;

class Node {
    private final InetAddress address;
    private final int port;

    public Node(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }
}
