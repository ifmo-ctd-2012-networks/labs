package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

class VersionMismatchException extends ParseException {

    public VersionMismatchException(int receivedVersion) {
        super("Version mismatch: " + receivedVersion + " not supported (current version: " + MessageService.PROTOCOL_VERSION + ")");
    }
}

