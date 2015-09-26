package ru.zyulyaev.ifmo.net.lab1.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zyulyaev
 */
public abstract class AbstractComponent implements Component {
    private static final Logger log = LoggerFactory.getLogger(AbstractComponent.class);

    private final AtomicReference<State> state = new AtomicReference<>(State.NONE);

    @Override
    public final void start() {
        if (!state.compareAndSet(State.NONE, State.STARTING))
            throw new IllegalStateException(getClass() + " is already " + state.get());
        try {
            startImpl();
            state.set(State.STARTED);
        } catch (ComponentInitializationException e) {
            log.error("Failed to start {}", getClass(), e);
            state.set(State.STOPPED);
        }
    }

    @Override
    public final void stop() {
        if (!state.compareAndSet(State.STARTED, State.STOPPED))
            throw new IllegalStateException(getClass() + " is not started yet");
        stopImpl();
    }

    protected abstract void startImpl() throws ComponentInitializationException;

    protected abstract void stopImpl();

    private enum State {
        NONE, STARTING, STARTED, STOPPED
    }
}
