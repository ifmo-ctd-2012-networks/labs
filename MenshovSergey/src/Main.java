import network.BroadcastReceiver;
import network.BroadcastSender;



/**
 * Created by sergej on 14.09.15.
 */


public class Main{

    public static void main(String[] args){
        System.out.println(Info.getHostName());
        BroadcastSender sender = new BroadcastSender(Info.getMacAddr(), Info.getHostName());
        new Thread(sender).start();
        BroadcastReceiver receiver = new BroadcastReceiver();
        Updater updater = new Updater(receiver);
        new Thread(receiver).start();

        updater.run();

    }

}