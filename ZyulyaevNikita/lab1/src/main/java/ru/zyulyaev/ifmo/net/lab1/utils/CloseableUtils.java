package ru.zyulyaev.ifmo.net.lab1.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zyulyaev
 */
public class CloseableUtils {
    private static final Logger log = LoggerFactory.getLogger(CloseableUtils.class);

    public static void tryClose(Closeable closeable, String error) {
        try {
            closeable.close();
        } catch (IOException e) {
            log.error(error, e);
        }
    }

    private CloseableUtils() {
    }
}
