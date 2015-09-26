package network;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by kerzo on 19.09.2015.
 */
public class MessageHandler extends TimerTask {
    private static int dividerWidth = 104;
    private ConcurrentSkipListSet<Node> sortedHosts;

    public MessageHandler(ConcurrentSkipListSet<Node> sortedHosts) {
        this.sortedHosts = sortedHosts;
    }

    @Override
    public void run() {
        printTable();
    }
    private String generateDivider() {
        String result = " ";
        for (int i = 0; i < dividerWidth; i++) {
            result += "-";
        }
        result += "\n";
        return result;
    }

    private void printTable() {
        StringBuilder sb = new StringBuilder();
        dividerWidth = sortedHosts.size() > 0 ? sortedHosts.first().toString().length() - 1 : dividerWidth;
        String divider = generateDivider();
        sb.append(divider);
        for (Node host : sortedHosts) {
            sb.append(host.toString()).append("\n");
            sb.append(divider);
        }
        System.out.println(sb.toString());
    }
}
