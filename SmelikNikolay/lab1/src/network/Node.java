package network;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

/**
 * Created by kerzo on 19.09.2015.
 */
public class Node implements Comparable<Node> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static int hostNameMaxLength;

    private Message message;
    private long lastRequest;

    private Queue<Long> missedRequest;

    public Node(Message message, long lastRequest) {
        this.message = message;
        this.lastRequest = lastRequest;
        missedRequest = new ArrayDeque<>();
        hostNameMaxLength = Math.max(hostNameMaxLength, message.getHostName().length());
    }

    public int getMissedRequestCount() {
        return missedRequest.size();
    }

    public void update(long curTime, Long timestamp, boolean received) {
        message.updateTimestamp(timestamp);
        if (curTime - lastRequest > 5000)
            missedRequest.add(curTime);

        if (received)
            lastRequest = curTime;

        while (!missedRequest.isEmpty() && curTime - missedRequest.peek() > 25000)
            missedRequest.poll();
    }

    public long getTimestamp() {
        return message.getTimestamp();
    }

    public String getMacAddress() {
        return message.getMacAddress();
    }

    public long getLastRequest() {
        return lastRequest;
    }

    @Override
    public String toString() {
        return " | MAC: " + Utils.fitToWidth(message.getMacAddress(), message.getMacAddress().length()) +
                " | HostName: " + Utils.fitToWidth(message.getHostName(), hostNameMaxLength) +
                " | Timestamp: " + Utils.fitToWidth(String.valueOf(message.getTimestamp()), 10) +
                " | Lost: " + Utils.fitToWidth(String.valueOf(getMissedRequestCount()), 2) +
                " | Last received: " + sdf.format(new Date(lastRequest)) +
                " |";

    }

    @Override
    public int compareTo(Node o) {
        if (o == null) {
            return 1;
        }
        return this.getMacAddress().compareTo(o.getMacAddress());
    }
}
