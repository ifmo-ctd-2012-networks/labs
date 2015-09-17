package ru.ifmo.loboda.net;

public class BadPacketException extends Exception {
    public BadPacketException(String message){
        super(message);
    }
}
