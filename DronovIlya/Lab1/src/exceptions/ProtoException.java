package exceptions;

public class ProtoException extends RuntimeException {

    public ProtoException() {
        super("Received invalid packet");
    }
}
