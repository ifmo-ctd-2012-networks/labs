package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import java.net.InetAddress;

interface State<D extends Data<D>> {
    D getData();

    int getTokenId();

    void rememberNode(InetAddress address, int tcpPort);
}
