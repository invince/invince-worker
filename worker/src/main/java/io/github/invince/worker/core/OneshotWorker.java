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
 * OneshotWorker process only one task, used in unlimited mode
 * @param <T> task type
 */
@Slf4j
public class OneshotWorker<T extends BaseTask> extends CompletableFuture<Void> implements Runnable {

    // from workerpool
    private final IToDoTasks toDo;

    // from workerpool
    private final IProcessingTasks<String, T> processing;

    // from workerpool
    private final ICompletableTaskFutureService completableTaskFutureService;

    public OneshotWorker(ICompletableTaskFutureService completableTaskFutureService, IToDoTasks toDo, IProcessingTasks<String, T> processing) {
        this.completableTaskFutureService = completableTaskFutureService;
        WorkerError.verify("Fail to init worker with null todo or null processing list")
                .nonNull(toDo, processing);
        this.toDo = toDo;
        this.processing = processing;
    }


    /**
     * process the task taken from todo list
     */
    @Override
    public void run() {
        try {
            BaseTask task = toDo.take();
            if (task != null && !(task instanceof FinishTask) && task.getKey() != null) {
                var taskFuture = completableTaskFutureService.getOrWrap(task);
                if (task.isToBeCancelled()) {
                    log.debug("{} has been already cancelled, we won't process it, " +
                            "stills has {} tasks in todo list", task.getUniqueKey(), toDo.size());
                    taskFuture.completeExceptionally(new ToDoTaskCancelled(task.getKey()));
                } else {
                    log.debug("{} starts at {}, stills has {} tasks in todo list", task.getUniqueKey(), ZonedDateTime.now(), toDo.size());
                    processing.put(task.getKey(), (T) task);
                    toDo.movedToProcessing(task.getKey());
                    task.process(taskFuture);
                    log.debug("{} finishes at {}, stills has {} tasks in processing", task.getUniqueKey(), ZonedDateTime.now(), processing.size());
                    processing.remove(task.getKey());
                    if (taskFuture.isToRetry()) {
                        toDo.add(task);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            this.completeExceptionally(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.completeExceptionally(e);
        } finally {
            log.info("[OneshotWorker]: task finishes");
            this.complete(null);
        }
    }
}
