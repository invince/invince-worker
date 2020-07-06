package com.invince.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class StandardWorker <T extends AbstractTask> implements Runnable {

    // from workerpool
    private final BlockingQueue<AbstractTask> toDo;

    // from workerpool
    private final ConcurrentHashMap<String, T> processing;

    private boolean finished = false;

    private int counter = 0;

    public StandardWorker(BlockingQueue<AbstractTask> toDo, ConcurrentHashMap<String, T> processing) {
        this.toDo = toDo;
        this.processing = processing;
    }

    @Override
    public void run() {

        try {
            AbstractTask task;
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
            e.printStackTrace();
        }
        finished = true;
    }

    public boolean finished() {
        return finished;
    }
}
