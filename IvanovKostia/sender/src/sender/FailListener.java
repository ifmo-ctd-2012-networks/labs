package sender;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface FailListener {
    void onFail(InetSocketAddress badNode);
}
