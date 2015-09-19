import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static void main(String[] args) throws UnknownHostException, SocketException, UnsupportedEncodingException, InterruptedException {
        ConcurrentLinkedQueue<Node> queue = new ConcurrentLinkedQueue<>();
        Printer printer = new Printer(queue);
        Thread threadPrinter = new Thread(printer);
        threadPrinter.start();

        Server server1 = new Server(8080,0);
        //Server server2 = new Server(8887,3);
        Client client1 = new Client(8080, 2, queue);
        //Client client2 = new Client(8882,700);
        //Client client3 = new Client(8883,1000);
        Thread clientThread1 = new Thread(client1);
        //Thread clientThread2 = new Thread(client2);
        //Thread clientThread3 = new Thread(client3);
        Thread serverThread1 = new Thread(server1);
        serverThread1.start();
        //Thread serverThread2 = new Thread(server2);
        //serverThread1.start();
        //serverThread2.start();
        //Thread.sleep(1000);
        clientThread1.start();
        //clientThread2.start();
        //clientThread3.start();
        /*
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println(inetAddress.getHostName());
        System.out.println(inetAddress.getCanonicalHostName());
        System.out.println(inetAddress.getHostAddress());
        */
        /*
        System.out.println(MacAddress.getMacAdress());
        byte[] mac = MacAddress.getMacAdress();
        String macString = getStringFromByteArray(mac);
        System.out.println(macString);
        */
    }

    public static String getStringFromByteArray(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(String.format("%02X%s", array[i], (i < array.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

}
