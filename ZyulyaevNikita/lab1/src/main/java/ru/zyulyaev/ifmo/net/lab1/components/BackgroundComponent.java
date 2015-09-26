package ru.zyulyaev.ifmo.net.lab1.components;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author zyulyaev
 */
public abstract class BackgroundComponent extends AbstractComponent {
    private final ExecutorService executorService;
    private Future<?> future;

    protected BackgroundComponent(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected BackgroundComponent() {
        this(Executors.newSingleThreadExecutor());
    }

    @Override
    protected final void startImpl() throws ComponentInitializationException {
        init();
        future = executorService.submit(this::doInBackground);
    }

    protected abstract void init() throws ComponentInitializationException;

    protected abstract void doInBackground();

    @Override
    protected final void stopImpl() {
        future.cancel(true);
        cleanup();
    }

    protected abstract void cleanup();
}
