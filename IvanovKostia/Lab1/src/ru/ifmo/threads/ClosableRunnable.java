package ru.ifmo.threads;

import java.io.Closeable;
import java.io.IOException;

public interface ClosableRunnable extends Runnable, Closeable {
    @Override
    default void close() throws IOException {
    }
}
