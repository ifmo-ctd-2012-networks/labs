package tr.core;

import java.util.Random;

public class Configuration {
    public static long INIT_TIME;
    public static long INIT_TIMEOUT;
    public static int BROADCAST_PORT;
    public static int SERVER_PORT;
    public static long TOKEN_TIMEOUT;
    public static long RESPONSE_TIMEOUT;
    public static long CHECKER_TIMEOUT;
    public static PayloadManager manager;
    public static PayloadProcessor processor;
    public static Random random;
    public static String tokenFile;
    public static String iface;
}
