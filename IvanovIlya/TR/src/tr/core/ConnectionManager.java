package tr.core;

import javafx.util.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ConnectionManager {
    private final Node node;
    private final StateChecker checker;
    private final InputManager inputManager;
    private final OutputManager outputManager;
    private final Map<InetAddress, Connection> connections = new HashMap<>();
    private volatile boolean isFinished;

    public ConnectionManager(Node node, StateChecker checker) throws IOException {
        this.node = node;
        this.checker = checker;
        inputManager = new InputManager();
        outputManager = new OutputManager();
    }

    public void start() {
        inputManager.start();
        outputManager.start();
    }

    public void send(InetAddress address, Message message) {
        if (node.isMyAddr(address)) {
            if (message.getType() < 2) {
                node.process(address, message);
            } else {
                checker.process(address, message);
            }
        } else {
            outputManager.send(address, message);
        }
    }

    public void finish() throws IOException {
        isFinished = true;
        inputManager.finish();
        outputManager.finish();
        for (Connection connection : connections.values()) {
            connection.finish();
        }
    }

    private class InputManager extends Thread {
        private final ServerSocket serverSocket;

        public InputManager() throws IOException {
            serverSocket = new ServerSocket(Configuration.SERVER_PORT);
        }

        @Override
        public void run() {
            while (!isFinished) {
                try {
                    Socket socket = serverSocket.accept();
                    InetAddress address = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
                    synchronized (connections) {
                        if (connections.containsKey(address)) {
                            connections.get(address).finish();
                        }
                        Connection connection = new Connection(node, checker, socket);
                        connections.put(address, connection);
                        connection.start();
                    }
                } catch (IOException e) {
                    //if (!isFinished)
                        //System.err.println("Error: " + e.getMessage());
                }
            }
        }

        public void finish() throws IOException {
            serverSocket.close();
            interrupt();
        }
    }

    private class OutputManager extends Thread {
        private final Queue<Pair<InetAddress, Message>> queue = new ArrayDeque<>();

        @Override
        public void run() {
            while (!isFinished) {
                Pair<InetAddress, Message> message;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    message = queue.poll();
                }
                synchronized (connections) {
                    try {
                        if (!connections.containsKey(message.getKey()) || connections.get(message.getKey()).isFinished()) {
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(message.getKey(), Configuration.SERVER_PORT));
                            Connection connection = new Connection(node, checker, socket);
                            connections.put(message.getKey(), connection);
                            connection.start();
                        }
                        connections.get(message.getKey()).send(message.getValue());
                    } catch (IOException e) {
                        //if (!isFinished)
                            //System.err.println("Error: " + e.getMessage() + " " + message.getKey().getCanonicalHostName());
                    }
                }
            }
        }

        public void send(InetAddress address, Message message) {
            synchronized (queue) {
                queue.add(new Pair<>(address, message));
                queue.notify();
            }
        }

        public void finish() {
            interrupt();
        }
    }
}
