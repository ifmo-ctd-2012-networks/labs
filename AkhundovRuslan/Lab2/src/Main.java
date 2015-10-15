public class Main {

    public static void main(String[] args) {

        Thread reconfigureReceiverThread = new Thread(new ReconfigureReceiver());
        Thread tokenSenderThread = new Thread(new TokenSender());
        Thread tokenReceiverThread = new Thread(new TokenReceiver());

        reconfigureReceiverThread.setDaemon(true);
        tokenSenderThread.setDaemon(true);
        tokenReceiverThread.setDaemon(true);

        reconfigureReceiverThread.start();
        tokenSenderThread.start();
        tokenReceiverThread.start();
    }
}
