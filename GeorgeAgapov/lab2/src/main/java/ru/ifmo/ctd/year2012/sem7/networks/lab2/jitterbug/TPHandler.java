package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import java.io.ObjectInputStream;
import java.util.List;

interface TPHandler {
    void handleTP1(int tokenId, int nodeListHash);

    void handleTP2();

    void handleTP3();

    void handleTP4(List<Node> nodes);

    void handleTP5(int tokenId, ObjectInputStream dataStream);
}
