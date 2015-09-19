package ru.ifmo.isomurodov.broadcast;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by javlon on 18.09.15.
 */
public class Main {
    public static final int TIME = 5000;
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Thread broadcaster = new Thread(new Sender(port));
        DB db = new DB();
        Thread listener = new Thread(new Receiving(port, db));
        broadcaster.start();
        listener.start();
        while(true){
            try {
                Thread.sleep(TIME);
                System.out.print("Time ");
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                System.out.println(dateFormat.format(date));
                db.tick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
