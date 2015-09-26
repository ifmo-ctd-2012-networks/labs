package com.lukin.network.lab1;

import java.io.PrintStream;

/**
 * Created by Саша on 19.09.2015.
 */
public class Printer extends LinkedBlockingQueueRunnable<Object> {
    private final PrintStream printStream;

    public Printer(PrintStream printStream) {
        this.printStream = printStream;
    }

    public void println(Object o){
        try {
            put(o);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void execute(Object o) {
        printStream.println(o);
    }
}
