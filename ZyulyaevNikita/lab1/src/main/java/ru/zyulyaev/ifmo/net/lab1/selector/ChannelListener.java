package ru.zyulyaev.ifmo.net.lab1.selector;

import java.nio.channels.SelectableChannel;

/**
 * @author zyulyaev
 */
public interface ChannelListener<T extends SelectableChannel> {
    /**
     * @return new interestOps or -1 if channel should be unregistered.
     */
    int onChannelReady(T channel, int interestOps, int readyOps);
}
