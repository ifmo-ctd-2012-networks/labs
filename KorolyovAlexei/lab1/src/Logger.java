package src;

import java.util.LinkedList;
import java.util.Queue;

public class Logger {

    private Logger() {
    }

    public static synchronized void commit(String tag, String message) {
        System.out.println(tag + " " + message);
    }

}
