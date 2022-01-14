package com.invince.worker;

import com.google.common.base.Stopwatch;
import com.invince.exception.InProgressingTaskCancelled;
import com.invince.exception.WorkerError;
import com.invince.exception.WorkerException;
import com.invince.spring.ContextHolder;
import com.invince.util.SafeRunner;
import com.invince.worker.future.ICompletableTaskService;
import com.invince.worker.future.local.DefaultCompletableTaskService;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class BaseTask<T> implements Serializable {

    protected ZonedDateTime queuedTime;
    protected ZonedDateTime startTime;
    protected ZonedDateTime processedTime;

    private final String defaultKey;
    private boolean toBeCancelled = false;


    @Accessors(chain = true)
    @Setter
    private boolean useCustomCompletableTaskService = false; // for ex, you can use redis version, but be careful, that will creates a lot of connection (almost one per task) to redis

    protected transient AtomicBoolean toContinue ;

    abstract void processInternal();
    protected void onEnqueue() {}
    protected void onStart() {}
    protected void onFinish() {}
    protected void onError(Exception e) {}

    protected void onCancelToDo() {}
    protected void onCancelProcessing() {}

    public BaseTask() {
        this.queuedTime = ZonedDateTime.now();
        this.defaultKey = UUID.randomUUID().toString();
    }

    public final void process() {
        Stopwatch timer = Stopwatch.createStarted();
        CompletableFuture<T> future = getFuture();
        future.exceptionally(ex -> {
            throw new WorkerException("Task failed: " + getKey(), ex);
        });
        try {
            this.startTime = ZonedDateTime.now();
            SafeRunner.run(this::onStart);
            processInternal();
            SafeRunner.run(this::onFinish);
            this.processedTime = ZonedDateTime.now();
            log.debug("{} {} takes: {}, Queued at: {}, Starts at: {}, Processed at: {}",
                    getClass().getSimpleName(), getKey(), timer.stop(), queuedTime, startTime, processedTime);
        } catch (InProgressingTaskCancelled | CancellationException e) {
            log.error(e.getMessage(), e);
            SafeRunner.run(this:: onCancelProcessing);
            future.completeExceptionally(new InProgressingTaskCancelled(getKey()));
            this.processedTime = ZonedDateTime.now();
            log.debug("{} {} takes: {}, Queued at: {}, Starts at: {}, but cancelled at: {}",
                    getClass().getSimpleName(), getKey(), timer.stop(), queuedTime, startTime, processedTime);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SafeRunner.run(() -> onError(e));
            future.completeExceptionally(new WorkerError(getKey() + " failed"));
            this.processedTime = ZonedDateTime.now();
            log.debug("{} {} takes: {}, Queued at: {}, Starts at: {}, but failed at: {}",
                    getClass().getSimpleName(), getKey(), timer.stop(), queuedTime, startTime, processedTime);
        } finally {
            if (!future.isDone() && !future.isCancelled() && !future.isCompletedExceptionally()) {
                log.error("{} {} is not done, not completed exceptionally and not cancelled, please check your code",
                        getClass().getSimpleName(), getKey());
                future.completeExceptionally(new WorkerError(getKey() + " failed"));
            }
        }
    }

    // you can override this
    public String getKey() {
        return defaultKey;
    }

    public CompletableFuture<T> getFuture() {
        ICompletableTaskService taskService = new DefaultCompletableTaskService();
        taskService = useCustomCompletableTaskService ? ContextHolder.getInstanceOrDefault(ICompletableTaskService.class, taskService) : taskService;
        return taskService.getOrWrap(this);
    }

    final void onEnqueueSafe() {
        SafeRunner.run(this::onEnqueue);
    }

    public boolean isToBeCancelled() {
        return toBeCancelled;
    }

    public synchronized final void cancelToDo() {
        toBeCancelled = true;
        onCancelToDo();
    }

    public synchronized final void cancelProcessing() {
        if(toContinue == null) {
            initToContinue();
        }
        toContinue.set(false);
    }

    // you can use this to break your process inside processInternal
    protected void checkPoint() {
        if(!toContinue()) {
            throw new InProgressingTaskCancelled(getKey());
        }
    }

    private boolean toContinue() {
        if(toContinue == null) {
            initToContinue();
        }
        return toContinue.get();
    }

    private synchronized void initToContinue() {
        if(toContinue == null) {
            toContinue = new AtomicBoolean(true);
        }
    }
}
