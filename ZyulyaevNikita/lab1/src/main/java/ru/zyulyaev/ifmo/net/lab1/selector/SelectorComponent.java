package ru.zyulyaev.ifmo.net.lab1.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zyulyaev.ifmo.net.lab1.components.BackgroundComponent;
import ru.zyulyaev.ifmo.net.lab1.components.ComponentInitializationException;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.zyulyaev.ifmo.net.lab1.utils.CloseableUtils.tryClose;

/**
 * @author zyulyaev
 */
public class SelectorComponent extends BackgroundComponent {
    private static final Logger log = LoggerFactory.getLogger(SelectorComponent.class);

    private Selector selector;

    public SelectorComponent(ExecutorService executorService) {
        super(executorService);
    }

    public SelectorComponent() {
        super(Executors.newSingleThreadExecutor());
    }

    @Override
    protected final void init() throws ComponentInitializationException {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            if (selector != null)
                tryClose(selector, "Failed to close selector");
            throw new ComponentInitializationException(e);
        }
    }

    public <T extends SelectableChannel> void registerChannel(T channel, int interestOps, ChannelListener<T> listener) throws ClosedChannelException {
        channel.register(selector, interestOps, listener);
    }

    @Override
    protected void doInBackground() {
        try {
            while (!Thread.interrupted()) {
                while (selector.select() != 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (SelectionKey key : keys) {
                        ChannelListener listener = (ChannelListener) key.attachment();
                        @SuppressWarnings("unchecked")
                        int interestOps = listener.onChannelReady(key.channel(), key.interestOps(), key.readyOps());
                        if (interestOps == -1) {
                            key.cancel();
                        } else {
                            //noinspection MagicConstant
                            key.interestOps(interestOps);
                        }
                    }
                    keys.clear();
                }
            }
        } catch (IOException e) {
            log.error("{} crashed", getClass(), e);
            stop();
        }
    }

    @Override
    protected final void cleanup() {
        tryClose(selector, "Failed to close selector");
    }
}
