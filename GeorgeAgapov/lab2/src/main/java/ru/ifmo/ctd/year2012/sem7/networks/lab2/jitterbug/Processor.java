package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class Processor<D extends Data<D>> extends Thread implements VariableHolder<D> {
    private final static Logger log = LoggerFactory.getLogger(MessageService.class);
    private final Context<D> context;

    @Getter
    private volatile D data;

    @Getter
    private volatile int tokenId;

    @Getter
    private final BlockingQueue<Event> queue;

    Processor(Context<D> context) {
        this.context = context;
        queue = new ArrayBlockingQueue<>(context.getSettings().getQueueCapacity());
        data = context.getSettings().getInitialData();
        tokenId = generateTokenId(false);
    }

    private int generateTokenId(boolean isLeader) {
        return 0;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if(Thread.currentThread().isInterrupted()){
                    Thread.interrupted();
                    break;
                }
                Event event = queue.take();
                if(event instanceof TPReceivedEvent){
                    Socket socket = ((TPReceivedEvent) event).getSocket();
                    //...
                }
            }
        } catch (InterruptedException e) {
            log.debug("Processor was interrupted");
        }
    }
}
