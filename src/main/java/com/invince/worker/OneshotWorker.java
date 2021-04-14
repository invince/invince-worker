package com.invince.worker;

import com.invince.worker.collections.SubscribableBlockingQueue;
import com.invince.worker.exception.WorkerError;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OneshotWorker<T extends BaseTask> extends CompletableFuture<Void> implements Runnable {

    // from workerpool
    private final SubscribableBlockingQueue<BaseTask> toDo;

    // from workerpool
    private final ConcurrentHashMap<String, T> processing;

    public OneshotWorker(SubscribableBlockingQueue<BaseTask> toDo, ConcurrentHashMap<String, T> processing) {
        this.toDo = toDo;
        this.processing = processing;
    }

    @Override
    public void run() {
        try {
            BaseTask task = toDo.take();
            if (task != null && !(task instanceof FinishTask) && task.getKey() != null) {
                log.debug("Task {} starts at {}, stills has {} tasks in todo list",
                        task.getKey(), ZonedDateTime.now(), toDo.size());
                processing.put(task.getKey(), (T) task);
                task.process();
                processing.remove(task.getKey());
                log.debug("Task {} finishes at {}, stills has {} tasks in processing",
                        task.getKey(), ZonedDateTime.now(), processing.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkerError(e.getMessage(), e);
        }
        log.info("[OneshotWorker]: task finishes");
        this.complete(null);
    }
}
