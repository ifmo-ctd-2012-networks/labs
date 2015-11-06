package tr.core;

import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Node extends Thread {
    private final Queue<Pair<InetAddress, Message>> queue = new ArrayDeque<>();
    private final List<Pair<InetAddress, List<Byte>>> network = new ArrayList<>();
    private volatile StateChecker checker;
    private volatile ConnectionManager manager;
    private Pair<InetAddress, Message> last;
    private volatile boolean isFinished;

    public void setChecker(StateChecker checker) {
        this.checker = checker;
    }

    public void setManager(ConnectionManager manager) {
        this.manager = manager;
    }

    private NetworkInterface getInterface() throws SocketException {
        if (Configuration.iface != null) {
            return NetworkInterface.getByName(Configuration.iface);
        }
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface i : interfaces) {
            if (i.getHardwareAddress() != null) {
                List<InterfaceAddress> add = i.getInterfaceAddresses();
                for (InterfaceAddress a : add) {
                    if (a.getBroadcast() != null) {
                        return i;
                    }
                }
            }
        }
        System.err.println("Fatal: No interface available");
        System.exit(1);
        throw new AssertionError();
    }

    private boolean initialize() throws IOException {
        File f = new File("state.txt");
        if (f.exists()) {
            Scanner sc = new Scanner(new File("state.txt"));
            while (sc.hasNext()) {
                try {
                    byte[] ip = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        ip[i] = sc.nextByte();
                    }
                    InetAddress address = InetAddress.getByAddress(ip);
                    List<Byte> m = new ArrayList<>();
                    for (int i = 0; i < 6; i++) {
                        m.add(sc.nextByte());
                    }
                    network.add(new Pair<>(address, m));
                } catch (UnknownHostException e) {
                    System.err.println("Fatal: " + e.getMessage());
                    System.exit(1);
                }
            }
            return false;
        } else {
            new Thread(() -> {
                long time1 = System.currentTimeMillis();
                try {
                    DatagramSocket socket = new DatagramSocket();
                    List<InetAddress> addresses = new ArrayList<>();
                    List<InterfaceAddress> add;
                    add = getInterface().getInterfaceAddresses();
                    byte[] mac = getInterface().getHardwareAddress();
                    addresses.addAll(add.stream().filter(a -> a.getBroadcast() != null).map(InterfaceAddress::getBroadcast).collect(Collectors.toList()));
                    while (System.currentTimeMillis() < time1 + Configuration.INIT_TIME) {
                        for (InetAddress address : addresses) {
                            socket.send(new DatagramPacket(mac, mac.length, address, Configuration.BROADCAST_PORT));
                        }
                        try {
                            Thread.sleep(Configuration.INIT_TIMEOUT);
                        } catch (InterruptedException e) {
                        }
                    }
                } catch (SocketException e) {
                    System.err.println("Fatal: " + e.getMessage());
                    System.exit(1);
                } catch (IOException e) {
                }
            }).start();
        }
        long time = System.currentTimeMillis();
        Map<List<Byte>, InetAddress> addresses = new HashMap<>();
        try {
            DatagramSocket socket = new DatagramSocket(Configuration.BROADCAST_PORT);
            while (true) {
                long t = System.currentTimeMillis();
                if (t >= time + Configuration.INIT_TIME) {
                    break;
                }
                try {
                    socket.setSoTimeout((int) (time + Configuration.INIT_TIME - t));
                    DatagramPacket packet = new DatagramPacket(new byte[6], 6);
                    socket.receive(packet);
                    List<Byte> mac = new ArrayList<>();
                    for (int i = 0; i < 6; i++) {
                        mac.add(packet.getData()[i]);
                    }
                    addresses.put(mac, packet.getAddress());
                } catch (IOException e) {
                }
            }
        } catch (SocketException e) {
            System.err.println("Fatal: " + e.getMessage());
            System.exit(1);
        }
        List<List<Byte>> am = new ArrayList<>();
        am.addAll(addresses.keySet());
        am.sort((o1, o2) -> {
            for (int i = 0; i < o1.size(); i++) {
                if (o1.get(i) > o2.get(i)) {
                    return -1;
                } else if (o1.get(i) < o2.get(i)) {
                    return 1;
                }
            }
            return 0;
        });
        network.addAll(am.stream().map(m -> new Pair<>(addresses.get(m), m)).collect(Collectors.toList()));
        Writer writer = new FileWriter("state.txt");
        for (Pair<InetAddress, List<Byte>> a : network) {
            for (int i = 0; i < 4; i++) {
                writer.write(a.getKey().getAddress()[i] + " ");
            }
            for (int i = 0; i < a.getValue().size(); i++) {
                writer.write(a.getValue().get(i) + " ");
            }
            writer.write("\n");
        }
        writer.close();
        return true;
    }

    private int getPosition() throws SocketException {
        List<Byte> m = new ArrayList<>();
        byte[] mac = getInterface().getHardwareAddress();
        for (byte aMac : mac) {
            m.add(aMac);
        }
        for (int i = 0; i < network.size(); i++) {
            if (network.get(i).getValue().equals(m)) {
                return i;
            }
        }
        throw new AssertionError();
    }

    private InetAddress getNext(boolean forward) throws SocketException {
        int i = getPosition();
        while (true) {
            i = (i + network.size() + (forward ? 1 : -1)) % network.size();
            if (!checker.contains(network.get(i).getKey())) {
                return network.get(i).getKey();
            }
        }
    }

    private Token getToken() throws IOException {
        File f = new File(Configuration.tokenFile);
        if (!f.exists()) {
            Writer writer = new FileWriter(f);
            writer.write("1\n");
            writer.close();
            return new Token(getInterface().getHardwareAddress(), 0);
        }
        Scanner sc = new Scanner(f);
        int v = sc.nextInt();
        Writer writer = new FileWriter(f);
        writer.write((v + 1) + "\n");
        writer.close();
        return new Token(getInterface().getHardwareAddress(), v);
    }

    private Pair<InetAddress, Message> getMessage(long wakeTime) {
        Pair<InetAddress, Message> message = null;
        synchronized (queue) {
            while (true) {
                long time = System.currentTimeMillis();
                if (!queue.isEmpty() || time >= wakeTime) {
                    break;
                }
                try {
                    queue.wait(wakeTime - time);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            if (!queue.isEmpty()) {
                message = queue.poll();
            }
        }
        return message;
    }

    @Override
    public void run() {
        boolean a = true;
        try {
            a = initialize();
        } catch (IOException e) {
            System.err.println("Fatal: " + e.getMessage());
            System.exit(1);
        }
        checker.start();
        try {
            if (getPosition() == 0 && a) {
                System.out.println("Starting...");
                send(new Message(Message.TOKEN_PASS, getToken(), true, Configuration.processor.getInitial()), true);
            }
            long time = System.currentTimeMillis();
            while (!isFinished) {
                Pair<InetAddress, Message> message = getMessage(time + Configuration.TOKEN_TIMEOUT);
                if (isFinished) {
                    return;
                }
                if (message == null) {
                    Object payload = last == null ? Configuration.processor.getInitial() : last.getValue().getPayload();
                    Token token = getToken();
                    //System.out.println("Generating new token: " + token);
                    send(new Message(Message.TOKEN_PASS, token, true, payload), true);
                    time = System.currentTimeMillis();
                } else if (message.getValue().getType() == Message.TOKEN_PASS) {
                    manager.send(message.getKey(), new Message(Message.TOKEN_RECEIVE, message.getValue().getToken()));
                    if (last == null || Configuration.manager.compare(last.getValue().getPayload(), message.getValue().getPayload()) >= 0) {
                        System.out.println("Token received: " + message.getValue().getToken());
                        boolean newForward = last == null || last.getValue().getToken().equals(message.getValue().getToken()) ?
                                Configuration.random.nextBoolean() : message.getValue().getForward();
                        Object payload = Configuration.processor.process(message.getValue().getPayload());
                        send(new Message(Message.TOKEN_PASS, message.getValue().getToken(), newForward, payload), newForward);
                        time = System.currentTimeMillis();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Fatal: " + e.getMessage());
            System.exit(1);
        }
    }

    private void send(Message toSend, boolean newForward) throws IOException {
        while (true) {
            synchronized (queue) {
                Pair<InetAddress, Message> best = null;
                while (!queue.isEmpty()) {
                    Pair<InetAddress, Message> next = queue.poll();
                    if (next.getValue().getType() == Message.TOKEN_PASS) {
                        manager.send(next.getKey(), new Message(Message.TOKEN_RECEIVE, next.getValue().getToken()));
                        best = best == null || Configuration.manager.compare(best.getValue().getPayload(), next.getValue().getPayload()) > 0 ? next : best;
                    }
                }
                if (best != null && Configuration.manager.compare(toSend.getPayload(), best.getValue().getPayload()) >= 0) {
                    queue.add(best);
                    break;
                }
            }
            InetAddress address = getNext(newForward);
            manager.send(address, toSend);
            long ct = System.currentTimeMillis();
            last = new Pair<>(address, toSend);
            while (true) {
                Pair<InetAddress, Message> msg = getMessage(ct + Configuration.RESPONSE_TIMEOUT);
                if (isFinished) {
                    return;
                }
                if (msg == null) {
                    checker.add(address);
                    toSend = new Message(Message.TOKEN_PASS, getToken(), toSend.getForward(), toSend.getPayload());
                    break;
                }
                if (msg.getValue().getType() == Message.TOKEN_PASS) {
                    manager.send(msg.getKey(), new Message(Message.TOKEN_RECEIVE, msg.getValue().getToken()));
                    if (Configuration.manager.compare(toSend.getPayload(), msg.getValue().getPayload()) >= 0) {
                        synchronized (queue) {
                            queue.add(msg);
                        }
                        return;
                    }
                } else if (msg.getKey().equals(address) && msg.getValue().getToken().equals(toSend.getToken())) {
                    return;
                }
            }
        }
    }

    public void process(InetAddress address, Message message) {
        synchronized (queue) {
            queue.add(new Pair<>(address, message));
            queue.notify();
        }
    }

    public boolean isMyAddr(InetAddress address) {
        try {
            return address.equals(network.get(getPosition()).getKey());
        } catch (SocketException e) {
            return false;
        }
    }

    public void finish() {
        isFinished = true;
        interrupt();
        checker.finish();
    }
}
