package ru.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.network.layer.ApplicationLayer;
import ru.network.state.InitializationState;
import ru.network.state.State;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author victor
 */
public class ServerNode extends Node implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ServerNode.class);
    private final Ring ring = new Ring(this);
    private ApplicationLayer applicationLayer;
    private String token;
    private String data = "";
    private long operationNumber;
    private StateLog stateLog = new StateLog(this);
    private State state;
    private Looper looper;

    public ServerNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            log.error("Usage: hostname port");
            System.exit(-1);
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        if (port < 1024) {
            log.error("Номер порта должен быть > 1024");
            System.exit(-1);
        }
        new Thread(new ServerNode(hostname, port), "main").start();
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

        looper.add(() -> setState(new InitializationState(this)));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                looper.add(() -> {
                    getState().tick();
                });
            }
        }, 0, 1000);

        Looper.loop();
    }

    public NodeStatus getStatus() {
        return state.getStatus();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (this.state != null) {
            this.state.leave();
        }
        this.state = state;
        this.state.enter();
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
