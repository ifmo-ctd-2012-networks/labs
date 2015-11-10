package sender;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface ReceiveListener<ReplyType extends Message> {
    void onReceive(InetSocketAddress address, ReplyType message);
}
