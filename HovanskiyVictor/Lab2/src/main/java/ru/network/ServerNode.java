package ru.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.layer.ApplicationLayer;
import ru.network.state.RecoveringState;
import ru.network.state.State;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author victor
 */
public class ServerNode extends Node implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Node.class);

    private ApplicationLayer applicationLayer;
    private NodeStatus status;
    private String token;
    private String data;
    private long operationNumber;
    private StateLog stateLog = new StateLog();

    private State state;
    private Looper looper;
    private final Ring ring = new Ring();

    public ServerNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        new Thread(new ServerNode(hostname, port), "main").start();
    }

    public void setState(State state) {
        if (this.state != null) {
            this.state.leave();
        }
        this.state = state;
        this.state.enter();
    }

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();

        applicationLayer = new ApplicationLayer(this);
        applicationLayer.connect();
        applicationLayer.listen(getPort(), false);
        applicationLayer.listen(ApplicationLayer.BROADCAST_PORT, true);
        this.macAddress = applicationLayer.getMacAddress().toString();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                looper.mQueue.add(() -> {
                    getState().tick();
                });
            }
        }, 0, 1000);

        looper.mQueue.add(() -> setState(new RecoveringState(this)));

        Looper.loop();
    }

    @Override
    public String toString() {
        return "Node[hostname = " + hostname + ", port = " + port + "]";
    }

    public NodeStatus getStatus() {
        return status;
    }

    public State getState() {
        return state;
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

    public boolean hasToken() {
        return token != null;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ApplicationLayer getApplicationLayer() {
        assert applicationLayer != null;
        return applicationLayer;
    }

    public StateLog getStateLog() {
        return stateLog;
    }

    public Ring getRing() {
        return ring;
    }
}
