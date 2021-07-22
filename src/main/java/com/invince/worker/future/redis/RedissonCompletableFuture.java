package com.invince.worker.future.redis;

import com.invince.exception.WorkerRemoteError;
import com.invince.worker.BaseTask;
import org.redisson.api.RedissonClient;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class RedissonCompletableFuture<T> extends CompletableFuture<T> {

    final RedissonClient redisson;
    final BaseTask<T> task;

    RedissonCompletableFuture(RedissonClient redisson, BaseTask<T> task) {
        this.redisson = redisson;
        this.task = task;
    }

    @Override
    public boolean isDone() {
        return !redisson.isShutdown() && getIsDoneSet().contains(task.getKey());
    }

    // 1. put key into getIsDoneSet
    // 2. put result into getResultBlockingQueue
    @Override
    public boolean complete(T value) {
        var rt = getIsDoneSet().add(task.getKey());
        return rt && getResultBlockingQueue().add(new RedissonCompletableFutureResultHolder<T>().setResult(value));
        
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        boolean rt = getIsCompletedExceptionallySet().add(task.getKey());
        return rt && getResultBlockingQueue().add(new RedissonCompletableFutureResultHolder<T>().setException(
                ex != null ? new WorkerRemoteError(ex.getMessage()) : new WorkerRemoteError()
        ));
    }

    @Override
    public boolean isCompletedExceptionally() {
        return !redisson.isShutdown() && getIsCompletedExceptionallySet().contains(task.getKey());
    }

    @Override
    public boolean isCancelled() {
        return !redisson.isShutdown() && getIsCancelledSet().contains(task.getKey());

    }

    private Set<String> getIsDoneSet() {
        return this.redisson.getSet(task.getClass().getSimpleName() + "$$isDone#");
    }


    private BlockingQueue<RedissonCompletableFutureResultHolder<T>> getResultBlockingQueue() {
        // the default jboss codec works bad if the class is in dependencies, we use jsonJacksonCodec instead
        return this.redisson.getBlockingQueue(task.getClass().getSimpleName() + "$$result#" + task.getKey(), 
                EnhancedJsonJacksonCodec.get());
    }

    private Set<String> getIsCompletedExceptionallySet() {
        return this.redisson.getSet(task.getClass().getSimpleName() + "$$isCompletedExceptionally#");
    }

    private Set<String> getIsCancelledSet() {
        return this.redisson.getSet(task.getClass().getSimpleName() + "$$isCancelled#");
    }

}
