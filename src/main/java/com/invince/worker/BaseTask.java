package com.invince.worker;

import com.google.common.base.Stopwatch;
import com.invince.worker.exception.WorkerError;
import com.invince.worker.exception.WorkerException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
abstract class BaseTask<T> extends CompletableFuture<T> {

    protected ZonedDateTime queuedTime;
    protected ZonedDateTime processedTime;

    private String defaultKey;

    public BaseTask() {
        this.queuedTime = ZonedDateTime.now();
        this.defaultKey = UUID.randomUUID().toString();
        this.exceptionally(ex -> {
            throw new WorkerException("Task failed: " + getKey(), ex);
        });
    }

    public final void process() {
        Stopwatch timer = Stopwatch.createStarted();
        try{
            processInternal();
            this.processedTime = ZonedDateTime.now();
        } catch (Exception e){
            log.error(e.getMessage(), e);
            this.completeExceptionally(new WorkerError(getKey() + " failed"));
        } finally {
            log.debug("{} takes: {}, Queued at: {}, Processed at: {}",
                    getClass().getSimpleName(), timer.stop(), queuedTime, processedTime);
            if(!isDone() && !isCancelled() && !isCompletedExceptionally()) {
                log.error("{} is not done, not completed exceptionally and not cancelled, please check your code");
                this.completeExceptionally(new WorkerError(getKey() + " failed"));
            }
        }
    }

    abstract void processInternal();

    // you can override this
    public String getKey() {
        return defaultKey;
    }
}
