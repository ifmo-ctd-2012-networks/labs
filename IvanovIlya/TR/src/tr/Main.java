package tr;

import tr.core.Configuration;
import tr.core.ConnectionManager;
import tr.core.Node;
import tr.core.StateChecker;

import java.io.IOException;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
        Configuration.INIT_TIME = 10000;
        Configuration.INIT_TIMEOUT = 500;
        Configuration.BROADCAST_PORT = 1234;
        Configuration.SERVER_PORT = 1235;
        Configuration.TOKEN_TIMEOUT = 8000;
        Configuration.RESPONSE_TIMEOUT = 1000;
        Configuration.CHECKER_TIMEOUT = 2000;
        Configuration.manager = new PM();
        Configuration.processor = new PP();
        Configuration.random = new Random();
        Configuration.tokenFile = "token.txt";
        Configuration.iface = args[0];
        Node node = new Node();
        StateChecker checker = new StateChecker();
        ConnectionManager manager = new ConnectionManager(node, checker);
        node.setChecker(checker);
        node.setManager(manager);
        checker.setManager(manager);
        node.start();
        manager.start();
    }
}
