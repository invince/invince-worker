package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StandardWorker <T extends BaseTask> extends CompletableFuture<Void> implements Runnable {

    // from workerpool
    private final BlockingQueue<BaseTask> toDo;

    // from workerpool
    private final ConcurrentHashMap<String, T> processing;

    private int counter = 0;

    public StandardWorker(BlockingQueue<BaseTask> toDo, ConcurrentHashMap<String, T> processing) {
        this.toDo = toDo;
        this.processing = processing;
    }

    @Override
    public void run() {
        try {
            BaseTask task;
            do{
                task = toDo.take();
                if(task != null && !(task instanceof FinishTask)) {
                    processing.put(task.getKey(), (T) task);
                    task.process();
                    processing.remove(task.getKey());
                    counter ++;
                }
            }while (task != null && !(task instanceof FinishTask));
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        log.info("[StandardWorker]: Finish flag received, worker will be shutdown, {} task processed.", counter);
        this.complete(null);
    }
}
