package com.invince.worker;

import com.invince.exception.WorkerError;
import com.invince.worker.collections.IProcessingTasks;
import com.invince.worker.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class OneshotWorker<T extends BaseTask> extends CompletableFuture<Void> implements Runnable {

    // from workerpool
    private final IToDoTasks toDo;

    // from workerpool
    private final IProcessingTasks<String, T> processing;

    public OneshotWorker(IToDoTasks toDo, IProcessingTasks<String, T> processing) {
        WorkerError.verify("Fail to init worker with null todo or null processing list")
                .nonNull(toDo, processing);
        this.toDo = toDo;
        this.processing = processing;
        this.toDo.subscribe(()-> {
            if(!isDone() && !isCompletedExceptionally() && !isCancelled()) {
                log.info("[OneshotWorker]: task finishes");
                this.complete(null);
            }
        });
    }


    @Override
    public void run() {
        try {
            BaseTask task = toDo.take();
            if (task != null && !(task instanceof FinishTask) && task.getKey() != null) {
                if (task.isToBeCancelled()) {
                    log.debug("Task {} has been already cancelled, we won't process it, " +
                            "stills has {} tasks in todo list", task.getKey(), toDo.size());
                } else {
                    log.debug("Task {} starts at {}, stills has {} tasks in todo list",
                            task.getKey(), ZonedDateTime.now(), toDo.size());
                    processing.put(task.getKey(), (T) task);
                    toDo.moveToProcessing(task.getKey());
                    task.process();
                    processing.remove(task.getKey());
                    log.debug("Task {} finishes at {}, stills has {} tasks in processing",
                            task.getKey(), ZonedDateTime.now(), processing.size());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkerError(e.getMessage(), e);
        }
        log.info("[OneshotWorker]: task finishes");
        this.complete(null);
    }
}
