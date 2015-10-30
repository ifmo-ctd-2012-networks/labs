package ru.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author victor
 */
public class Token {
    private static final Logger log = LoggerFactory.getLogger(Token.class);

    public static String generate() {
        String value = UUID.randomUUID().toString();
        log.info("Сгенерирован новый токен " + value);
        return value;
    }
}
