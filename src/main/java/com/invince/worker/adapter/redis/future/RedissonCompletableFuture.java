package com.invince.worker.adapter.redis.future;

import com.invince.worker.core.ITaskContext;
import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.Set;

@Slf4j
public class RedissonCompletableFuture<T> extends CompletableTaskFuture<T> {

    private static final String IS_DONE = "$$isDone#";
    private static final String IS_COMPLETED_EXCEPTIONALLY = "$$isCompletedExceptionally#";
    private static final String IS_CANCELLED = "$$isCancelled#";

    private final RedissonClient redisson;
    private final RedissonCompletableTaskFutureHelper helper;

    RedissonCompletableFuture(RedissonClient redisson, RedissonCompletableTaskFutureHelper helper, ITaskContext taskContext) {
        super(taskContext);
        this.redisson = redisson;
        this.helper = helper;
    }

    @Override
    public T join() {
        return helper.join(this);
    }

    @Override
    public boolean isDone() {
        return !redisson.isShutdown() && getIsDoneSet().contains(getKey());
    }

    // 1. put key into getIsDoneSet
    // 2. put result into getResultBlockingQueue
    @Override
    public boolean complete(T value) {
        try {
            var rt = getIsDoneSet().add(getKey());
            return rt &&  helper.complete(this, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        try {
            boolean rt = getIsCompletedExceptionallySet().add(getKey());
            return rt && helper.completeExceptionally(this, ex);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isCompletedExceptionally() {
        return !redisson.isShutdown() && getIsCompletedExceptionallySet().contains(getKey());
    }

    @Override
    public boolean isCancelled() {
        return !redisson.isShutdown() && getIsCancelledSet().contains(getKey());

    }

    private Set<String> getIsDoneSet() {
        return this.redisson.getSet(getPrefix() + IS_DONE);
    }


    private Set<String> getIsCompletedExceptionallySet() {
        return this.redisson.getSet(getPrefix() + IS_COMPLETED_EXCEPTIONALLY);
    }

    private Set<String> getIsCancelledSet() {
        return this.redisson.getSet(getPrefix() + IS_CANCELLED);
    }

}
