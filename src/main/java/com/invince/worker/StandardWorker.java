package com.invince.worker;

import com.invince.exception.ToDoTaskCancelled;
import com.invince.exception.WorkerError;
import com.invince.worker.collections.IProcessingTasks;
import com.invince.worker.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class StandardWorker<T extends BaseTask> extends CompletableFuture<Void> implements Runnable {

    // from workerpool
    private final IToDoTasks toDo;

    // from workerpool
    private final IProcessingTasks<String, T> processing;

    private int counter = 0;

    public StandardWorker(IToDoTasks toDo, IProcessingTasks<String, T> processing) {
        WorkerError.verify("Fail to init worker with null todo or null processing list")
                .nonNull(toDo, processing);
        this.toDo = toDo;
        this.processing = processing;
        this.toDo.subscribe(() -> {
            if (!isDone() && !isCompletedExceptionally() && !isCancelled()) {
                log.info("[StandardWorker]: Finish flag received, worker will be shutdown, {} task processed.", counter);
                this.complete(null);
            }
        });
    }


    @Override
    public void run() {
        try {
            BaseTask task;
            do {
                task = toDo.take();
                if (task != null && !(task instanceof FinishTask) && task.getKey() != null) {
                    try {
                        log.debug("{} {} starts at {}, stills has {} tasks in todo list", task.getClass().getSimpleName(),
                                task.getKey(), ZonedDateTime.now(), toDo.size());
                        processing.put(task.getKey(), (T) task);
                        toDo.moveToProcessing(task.getKey());
                        if (task.isToBeCancelled()) {
                            log.debug("{} {} has already been cancelled, we won't process it, stills has {} tasks in processing",
                                    task.getClass().getSimpleName(), task.getKey(), processing.size());
                            task.getFuture().completeExceptionally(new ToDoTaskCancelled(task.getKey()));
                        } else {
                            task.process();
                            log.debug("{} {} finishes at {}, stills has {} tasks in processing",
                                    task.getClass().getSimpleName(), task.getKey(), ZonedDateTime.now(), processing.size());
                        }
                        processing.remove(task.getKey());
                        counter++;
                    } catch (Exception e) {
                        if(e instanceof InterruptedException) {
                            throw e;
                        }
                        log.error(e.getMessage(), e);
                    }
                }
            } while (task != null && !(task instanceof FinishTask));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkerError(e.getMessage(), e);
        }
        log.info("[StandardWorker]: Finish flag received, worker will be shutdown, {} task processed.", counter);
        this.complete(null);
    }
}
