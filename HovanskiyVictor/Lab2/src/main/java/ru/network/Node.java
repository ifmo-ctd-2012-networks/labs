package ru.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.state.NormalState;
import ru.network.state.ViewChangingState;

/**
 * @author victor
 */
public class Node implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Node.class);
    private final String hostname;
    private final int port;

    private final NormalState normal = new NormalState(this);
    private final ViewChangingState viewChanging = new ViewChangingState(this);
    private final Wrapper wrapper = new Wrapper(this);

    private NodeStatus status;
    private String token;
    private String data;
    private long operationNumber;

    public Node(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        new Node("victor", 777).run();
    }

    @Override
    public void run() {
        log.info(this + " is running...");
    }

    @Override
    public String toString() {
        return "a.Node[hostname = " + hostname + ", port = " + port + "]";
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public long getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(long operationNumber) {
        this.operationNumber = operationNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }
}
