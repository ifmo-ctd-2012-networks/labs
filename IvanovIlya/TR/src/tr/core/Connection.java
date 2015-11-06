package tr.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

public class Connection {
    private final Node node;
    private final StateChecker checker;
    private final Socket socket;
    private final InputConnection inputConnection;
    private final OutputConnection outputConnection;
    private volatile boolean isFinished;

    public Connection(Node node, StateChecker checker, Socket socket) {
        this.node = node;
        this.checker = checker;
        this.socket = socket;
        inputConnection = new InputConnection();
        outputConnection = new OutputConnection();
    }

    public void start() {
        inputConnection.start();
        outputConnection.start();
    }

    public void send(Message message) {
        outputConnection.send(message);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void finish() throws IOException {
        isFinished = true;
        socket.close();
        inputConnection.interrupt();
        outputConnection.interrupt();
    }

    private class InputConnection extends Thread {
        @Override
        public void run() {
            try {
                Scanner sc = new Scanner(socket.getInputStream());
                while (!isFinished) {
                    try {
                        Message message = Message.readMessage(sc);
                        if (message.getType() < 2) {
                            node.process(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress(), message);
                        } else {
                            checker.process(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress(), message);
                        }
                    } catch (Exception e) {
                        finish();
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                //System.err.println("Error: " + e.getMessage());
                try {
                    finish();
                } catch (IOException e1) {
                    //e.printStackTrace();
                    //System.err.println("Error: " + e1.getMessage());
                }
            }
        }
    }

    private class OutputConnection extends Thread {
        private final Queue<Message> queue = new ArrayDeque<>();

        @Override
        public void run() {
            try {
                Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                while (!isFinished) {
                    Message message;
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
                    message.writeMessage(writer);
                    writer.flush();
                }
            } catch (IOException e) {
                //System.err.println("Error: " + e.getMessage());
                //e.printStackTrace();
                try {
                    finish();
                } catch (IOException e1) {
                    //e.printStackTrace();
                    //System.err.println("Error: " + e1.getMessage());
                    //e.printStackTrace();
                }
            }
        }

        public void send(Message message) {
            synchronized (queue) {
                queue.add(message);
                queue.notify();
            }
        }
    }
}
