package exceptions;

public class ProtoException extends Exception {

    public ProtoException() {
        super("Received invalid packet");
    }
}
