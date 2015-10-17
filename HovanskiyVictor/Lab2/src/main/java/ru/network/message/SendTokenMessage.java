package ru.network.message;

/**
 * @author victor
 */
public class SendTokenMessage extends Message {
    private String token;
    private String data;
    private long operationNumber;

    public long getOperationNumber() {
        return operationNumber;
    }

    public String getToken() {
        return token;
    }

    public String getData() {
        return data;
    }
}
