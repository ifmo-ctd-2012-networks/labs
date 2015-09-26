package ru.zyulyaev.ifmo.net.lab1;

/**
* @author zyulyaev
*/
public class MessageMalformed extends Exception {
    public MessageMalformed() {
    }

    public MessageMalformed(String message) {
        super(message);
    }

    public MessageMalformed(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageMalformed(Throwable cause) {
        super(cause);
    }

    public MessageMalformed(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
