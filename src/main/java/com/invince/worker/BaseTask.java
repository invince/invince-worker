package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
abstract class BaseTask<T> extends CompletableFuture<T> {

    private String defaultKey;

    public BaseTask() {
        this.defaultKey = UUID.randomUUID().toString();
        this.exceptionally(ex -> {
            throw new WorkerException("Task failed: " + getKey(), ex);
        });
    }

    public final void process() {
        try{
            doProcess();
            doComplete();
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    abstract void doComplete();

    protected abstract void doProcess();

    // you can override this
    public String getKey() {
        return defaultKey;
    }
}
