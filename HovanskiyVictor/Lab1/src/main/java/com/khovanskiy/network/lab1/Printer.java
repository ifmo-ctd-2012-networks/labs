package com.khovanskiy.network.lab1;

import java.io.PrintStream;

/**
 * @author victor
 */
public class Printer extends QueueRunnable<Object> {

    private final PrintStream out;

    public Printer(PrintStream out) {
        this.out = out;
    }

    public void println(Object obj) {
        try {
            add(obj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handle(Object element) {
        out.println(element);
    }
}
