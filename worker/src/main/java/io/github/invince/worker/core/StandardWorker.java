package io.github.invince.worker.core;

import io.github.invince.exception.ToDoTaskCancelled;
import io.github.invince.exception.WorkerError;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Standard worker, keep processing the task taken from todo list until receiving FinishTask
 * @param <T> task type
 */
@Slf4j
public class StandardWorker<T extends BaseTask> extends CompletableFuture<Void> implements Runnable {

    // from workerpool
    private final IToDoTasks toDo;

    // from workerpool
    private final IProcessingTasks<String, T> processing;

    // from workerpool
    private final ICompletableTaskFutureService completableTaskFutureService;

    private int counter = 0;

    public StandardWorker(ICompletableTaskFutureService completableTaskFutureService, IToDoTasks toDo, IProcessingTasks<String, T> processing) {
        this.completableTaskFutureService = completableTaskFutureService;
        WorkerError.verify("Fail to init worker with null todo or null processing list")
                .nonNull(toDo, processing);
        this.toDo = toDo;
        this.processing = processing;
    }


    /**
     * keep processing the task taken from todo list until receiving FinishTask
     */
    @Override
    public void run() {
        try {
            BaseTask task;
            do {
                task = toDo.take();
                if (task != null && !(task instanceof FinishTask) && task.getKey() != null) {
                    try {
                        var taskFuture = completableTaskFutureService.getOrWrap(task);
                        log.debug("{} starts at {}, stills has {} tasks in todo list", task.getUniqueKey(), ZonedDateTime.now(), toDo.size());
                        processing.put(task.getKey(), (T) task);
                        toDo.movedToProcessing(task.getKey());
                        if (task.isToBeCancelled()) {
                            log.debug("{} has already been cancelled, we won't process it, stills has {} tasks in processing",
                                    task.getUniqueKey(), processing.size());
                            taskFuture.completeExceptionally(new ToDoTaskCancelled(task.getKey()));
                        } else {
                            task.process(taskFuture);
                            log.debug("{} finishes at {}, stills has {} tasks in processing",
                                    task.getUniqueKey(), ZonedDateTime.now(), processing.size());
                        }
                        processing.remove(task.getKey());
                        counter++;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } while (task != null && !(task instanceof FinishTask));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkerError(e.getMessage(), e);
        } finally {
            log.info("[StandardWorker]: Finish flag received, worker will be shutdown, {} task processed.", counter);
            this.complete(null);
        }

    }
}
