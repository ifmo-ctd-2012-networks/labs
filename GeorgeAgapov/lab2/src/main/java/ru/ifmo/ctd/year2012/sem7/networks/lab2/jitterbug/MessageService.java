package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

class MessageService<D extends Data<D>> {
    private final static Logger log = LoggerFactory.getLogger(MessageService.class);
    static final short PROTOCOL_VERSION = 1;
    private final Context<D> context;

    public MessageService(Context<D> context) {
        this.context = context;
    }

    public void handleTPMessage(byte[] data, TPHandler handler) throws ParseException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            MessageType type = handle(ois);
            switch (type) {
                case TP1:
                    handler.handleTP1(ois.readInt(), ois.readInt());
                    break;
                case TP2:
                    handler.handleTP2();
                    break;
                case TP3:
                    handler.handleTP3();
                    break;
                case TP4:
                    handler.handleTP4(parseNodeList(ois));
                    break;
                case TP5:
                    handler.handleTP5(ois.readInt(), ois);
                    break;
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    private List<Node> parseNodeList(ObjectInputStream ois) throws IOException {
        int size = ois.readInt();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            nodes.add(parseNode(ois));
        }
        return nodes;
    }

    private Node parseNode(ObjectInputStream ois) throws IOException {
        byte meta = ois.readByte();
        byte[] ipAddress;
        if ((meta & 1) == 0) {
            ipAddress = new byte[4];
        } else {
            ipAddress = new byte[16];
        }
        ois.readFully(ipAddress);
        InetAddress address = InetAddress.getByAddress(ipAddress);
        return new Node(address, ois.readUnsignedShort());
    }

    public void handleTRMessage(DatagramPacket packet, TRHandler trHandler) throws ParseException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength()));
            MessageType type = handle(ois);
            switch (type) {
                case TR1:
                    trHandler.handleTR1(packet.getAddress(), packet.getPort(), ois.readInt());
                    break;
                case TR2:
                    trHandler.handleTR2(packet.getAddress(), packet.getPort(), ois.readInt());
                    break;
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    private MessageType handle(ObjectInputStream ois) throws ParseException, IOException {
        short version = ois.readShort();
        if (PROTOCOL_VERSION != version) {
            throw new VersionMismatchException(version);
        }
        int typeCode = ois.readShort();
        MessageType type = MessageType.forCode(typeCode);
        if (type == null) {
            throw new ParseException("Unknown type code: " + typeCode);
        }
        log.debug("Handling {} message", type);
        return type;
    }

}
