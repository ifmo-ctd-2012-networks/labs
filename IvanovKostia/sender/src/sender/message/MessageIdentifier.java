package sender.message;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helps to determine for specified response-message corresponding request-message.
 */
class MessageIdentifier {
    private static final AtomicInteger counter = new AtomicInteger();

    private final int id;

    public MessageIdentifier() {
        id = counter.getAndIncrement();
    }
}
