package ru.ifmo.loboda.net;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IOThread implements Runnable {
    private final DB db;
    public static final int SLEEPTIME = 5000;

    public IOThread(DB db){
        this.db = db;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(SLEEPTIME);
                System.out.print("Snapshot at ");
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                System.out.println(dateFormat.format(date));
                db.tick(new PrintWriter(System.out));
                System.out.println();
                System.out.flush();
            } catch (InterruptedException e) {
                System.err.println("Interrupted");
                System.exit(1);
            }
        }
    }
}
