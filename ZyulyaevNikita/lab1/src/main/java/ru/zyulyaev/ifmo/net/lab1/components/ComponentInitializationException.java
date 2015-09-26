package ru.zyulyaev.ifmo.net.lab1.components;

/**
 * @author zyulyaev
 */
public class ComponentInitializationException extends Exception {
    public ComponentInitializationException() {
    }

    public ComponentInitializationException(String message) {
        super(message);
    }

    public ComponentInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComponentInitializationException(Throwable cause) {
        super(cause);
    }

    public ComponentInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
