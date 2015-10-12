package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import java.net.InetAddress;

interface TRHandler {
    void handleTR1(InetAddress address, int port, int tokenId);

    void handleTR2(InetAddress address, int port, int tokenId);
}
