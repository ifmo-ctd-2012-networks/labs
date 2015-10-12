package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import lombok.Getter;

import java.net.Socket;

class TPReceivedEvent implements Event{
    @Getter
    private final Socket socket;

    public TPReceivedEvent(Socket socket) {
        this.socket = socket;
    }
}
