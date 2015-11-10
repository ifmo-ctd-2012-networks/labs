package sender;

import sender.message.Cancellation;
import sender.message.Message;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * API for Replica (lower) layer.
 */
public class ReplicaSender {
    /**
     * Simply sends message, waits for result during some sensible time.
     * <p>
     * Current thread is blocked during method call.
     *
     * @param address     receiver of message
     * @param message     mail entry
     * @param type        way of sending a message: TCP, single UPD...
     * @param timeout     timeout in milliseconds
     * @param <ReplyType> response message type
     * @return response message
     * @throws SendingException when timeout exceeded
     */
    public <ReplyType extends Message> ReplyType sendAndExpect(InetSocketAddress address, Message<ReplyType> message, DispatchType type, int timeout) throws SendingException {
        return sendAndWait(address, message, type, timeout)
                .orElseThrow(() -> new SendingException(address));
    }

    /**
     * Same as <tt>sendAndExpect</tt>, but in case of no answer returns empty Optional instead of throwing exception
     */
    public <ReplyType extends Message> Optional<ReplyType> sendAndWait(InetSocketAddress address, Message<ReplyType> message, DispatchType type, int timeout) {
        // IMPLEMENTATION NOTES
        // this can be easily implemented via <tt>send</tt>

    }


    /**
     * Sends a message.
     * <p>
     * Current thread is NOT blocked by this method call.
     * But no two response-actions (onReceive or onFail on any request) or response protocols will be executed at same time,
     * so you can write not thread-safe code inside them.
     * <p>
     * <p>
     * This gets being very similar to automaton programming :)
     *
     * @param address     receiver of message
     * @param message     mail entry
     * @param type        way of sending a message: TCP, single UPD...
     * @param timeout     timeout in milliseconds
     * @param onReceive   an action to invoke when got an answer.
     * @param onFail      an action to invoke when timeout exceeded.
     * @param <ReplyType> response message type
     */
    public <ReplyType extends Message> void send(
            InetSocketAddress address,
            Message<ReplyType> message,
            DispatchType type,
            int timeout,
            ReceiveListener<ReplyType> receiveListener,
            FailListener failListener
    ) {
        // IMPLEMENTATION NOTES
        // this may help:
        // https://github.com/google/guava/wiki/ListenableFutureExplained

    }

    /**
     * Sends a broadcast, and returns stream of answers which would be collected during timeout.
     * <p>
     * Node will NOT receive its own request
     * (i.e. response protocols of this sender will ignore any broadcast from itself (or from sender with same address?)).
     * <p>
     * Note, that this method doesn't block the thread, but accessing elements of result stream does (in lazy way).
     *
     * @param message     mail entry
     * @param timeout     timeout in milliseconds
     * @param <ReplyType> responses type
     * @return stream of replies
     */
    public <ReplyType extends Message> Stream<ReplyType> broadcastAndWait(Message<ReplyType> message, int timeout) {
        // IMPLEMENTATION NOTES
        // about constructing return value:
        // there is a way to create a custom stream from an iterator

    }

    /**
     * Sends a broadcast.
     * <p>
     * Node will NOT receive its own request.
     * (i.e. response protocols of this sender will ignore any broadcast from itself (or from sender with same address?)).
     * <p>
     * Current thread is NOT blocked by this method call.
     * But no two response-actions (onReceive or onFail on any request) or response protocols will be executed at same time,
     * so you can write not thread-safe code inside them.
     *
     * @param message     mail entry
     * @param timeout     timeout in milliseconds
     * @param onReceive   is executed when get a response
     * @param onFail      if no answers received
     * @param <ReplyType> response type
     */
    public <ReplyType extends Message> void broadcast(Message<ReplyType> message, int timeout, ReceiveListener<ReplyType> receiveListener, FailListener failListener) {

    }

    /**
     * Determines behaviour on receiving request-message of specified type.
     * <p>
     * No any two response protocols or response-actions will be executed at the same time.
     *
     * @param protocol way on response on specified request-message
     * @return function to unregister this protocol.
     */
    public <Q extends Message<A>, A extends Message> Cancellation registerReplyProtocol(ReplyProtocol<Q, A> protocol) {

    }

    /**
     * Freezes request-messages receiver.
     * <p>
     * In frozen state no any response protocol is activated, all received request-messages are stored and not processed
     * until unfreezing. So you can safely change response protocols without scaring of missing any request.
     * <p>
     * Sender is initiated in frozen state
     * <p>
     * Call of this method also destroys all registered response protocols and response-actions of send- and broadcastAndWait
     * methods (optional feature)
     */
    public void freeze() {

    }

    /**
     * Unfreezes request-messages receiver. Messages received in frozen state begin to be processed.
     */
    public void unfreeze() {

    }


    public enum DispatchType {
        PLAIN, // UDP
        SAFE, // TCP
        LOOPBACK // don't laugh, it may be really essential (could be implemented without actual sending, could not)
    }

}
