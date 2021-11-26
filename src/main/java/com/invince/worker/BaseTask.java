package com.invince.worker;

import com.google.common.base.Stopwatch;
import com.invince.exception.WorkerError;
import com.invince.exception.WorkerException;
import com.invince.spring.ContextHolder;
import com.invince.util.SafeRunner;
import com.invince.worker.future.ICompletableTaskService;
import com.invince.worker.future.local.DefaultCompletableTaskService;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class BaseTask<T> implements Serializable {

    protected ZonedDateTime queuedTime;
    protected ZonedDateTime processedTime;

    private final String defaultKey;

    abstract void processInternal();

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
        try{
            SafeRunner.run(this::onStart);
            processInternal();
            this.processedTime = ZonedDateTime.now();
            SafeRunner.run(this::onFinish);
        } catch (Exception e){
            log.error(e.getMessage(), e);
            SafeRunner.run(() -> onError(e));
            future.completeExceptionally(new WorkerError(getKey() + " failed"));
        } finally {
            log.debug("{} takes: {}, Queued at: {}, Processed at: {}",
                    getClass().getSimpleName(), timer.stop(), queuedTime, processedTime);
            if(!future.isDone() && !future.isCancelled() && !future.isCompletedExceptionally()) {
                log.error("{} is not done, not completed exceptionally and not cancelled, please check your code");
                future.completeExceptionally(new WorkerError(getKey() + " failed"));
            }
        }
    }

    // you can override this
    public String getKey() {
        return defaultKey;
    }

    public CompletableFuture<T> getFuture() {
        return ContextHolder.getInstanceOrDefault(ICompletableTaskService.class, new DefaultCompletableTaskService())
                .getOrWrap(this);
    }

    final void onEnqueueSafe() {
        SafeRunner.run(this::onEnqueue);
    }

    protected void onEnqueue() {}
    protected void onStart() {}
    protected void onFinish() {}
    protected void onError(Exception e) {}

}
