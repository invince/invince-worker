package io.github.invince.worker.core;

import com.google.common.base.Stopwatch;
import io.github.invince.exception.InProgressingTaskCancelled;
import io.github.invince.exception.WorkerError;
import io.github.invince.exception.WorkerException;
import io.github.invince.util.SafeRunner;
import io.github.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BaseTask which can be handled by standard worker
 * @param <T> type of the result
 */
@Slf4j
public abstract class BaseTask<T> implements ITaskIdentify, Serializable {

    protected ZonedDateTime queuedTime;
    protected ZonedDateTime startTime;
    protected ZonedDateTime processedTime;

    private final String defaultKey;
    private AtomicBoolean toBeCancelled = new AtomicBoolean(false);
    protected AtomicBoolean toContinue = new AtomicBoolean(true) ;

    abstract void processInternal(CompletableTaskFuture<T> taskFuture);

    /**
     * Be called when task is enqueued
     */
    protected void onEnqueue() {}

    /**
     * Be called when task starts
     */
    protected void onStart() {}

    /**
     * Be called when task finishes
     */
    protected void onFinish() {}

    /**
     * Be called when task on error
     * @param e the exception you catched
     */
    protected void onError(Exception e) {}

    /**
     * Be called when task cancelled before it starts
     */
    protected void onCancelToDo() {}

    /**
     * Be called when task cancelled when it's in processing
     */
    protected void onCancelProcessing() {}

    /**
     * Be called when task is really cancelled
     */
    protected void onTaskCancelled() {}

    public BaseTask() {
        this.queuedTime = ZonedDateTime.now();
        this.defaultKey = UUID.randomUUID().toString();
    }

    /**
     * Process the task
     * @param taskFuture the future to control the status of the task
     */
    public final void process(CompletableTaskFuture<T> taskFuture) {
        Stopwatch timer = Stopwatch.createStarted();
        taskFuture.exceptionally(ex -> {
            throw new WorkerException(getUniqueKey() + " failed: ", ex);
        });
        try {
            this.startTime = ZonedDateTime.now();
            SafeRunner.run(this::onStart);
            processInternal(taskFuture);
            SafeRunner.run(this::onFinish);
            this.processedTime = ZonedDateTime.now();
            log.debug("{} takes: {}, Queued at: {}, Starts at: {}, Processed at: {}",
                    getUniqueKey(), timer.stop(), queuedTime, startTime, processedTime);
        } catch (InProgressingTaskCancelled | CancellationException e) {
            log.error(e.getMessage(), e);
            SafeRunner.run(this:: onTaskCancelled);
            taskFuture.completeExceptionally(new InProgressingTaskCancelled(getKey()));
            this.processedTime = ZonedDateTime.now();
            log.debug("{} takes: {}, Queued at: {}, Starts at: {}, but cancelled at: {}",
                    getUniqueKey(), timer.stop(), queuedTime, startTime, processedTime);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SafeRunner.run(() -> onError(e));
            taskFuture.completeExceptionally(new WorkerError(getKey() + " failed"));
            this.processedTime = ZonedDateTime.now();
            log.debug("{} takes: {}, Queued at: {}, Starts at: {}, but failed at: {}",
                    getUniqueKey(), timer.stop(), queuedTime, startTime, processedTime);
        } finally {
            if (!taskFuture.isDone() && !taskFuture.isCancelled() && !taskFuture.isCompletedExceptionally()) {
                log.error("{} is not done, not completed exceptionally and not cancelled, please check your code",
                        getUniqueKey());
                taskFuture.completeExceptionally(new WorkerError(getKey() + " failed"));
            }
        }
    }

    /**
     * you can override this
     * @return Key of your task
     */
    public String getKey() {
        return defaultKey;
    }

    /**
     * @return prefix of your task, to reduce the defaultKey collision
     */
    public String getPrefix() {
        return getClass().getCanonicalName();
    }

    /**
     *
     * @return check if task is to be cancelled when it's still in todo list
     */
    public boolean isToBeCancelled() {
        return toBeCancelled.get();
    }

    /**
     *
     * cancel the task when it's still in todo list
     */
    public synchronized final void cancelToDo() {
        toBeCancelled.set(true);
        onCancelToDo();
    }

    /**
     *
     * cancel the task when it's in progressing
     */
    public synchronized final void cancelProcessing() {
        toContinue.set(false);
        onCancelProcessing();
    }

    /**
     *
     * checkPoint to help you stop the process, we don't want kill the process violently
     */
    public void checkPoint() {
        if(!toContinue.get()) {
            throw new InProgressingTaskCancelled(getKey());
        }
    }

    final void onEnqueueSafe() {
        SafeRunner.run(this::onEnqueue);
    }
}
