package com.invince.worker.adapter.redis.future;

import com.invince.worker.core.ITaskIdentify;
import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.Set;

/**
 * Redis version of CompletableTaskFuture
 * @param <T> result type
 */
@Slf4j
public class RedissonCompletableFuture<T> extends CompletableTaskFuture<T> {

    private static final String IS_DONE = "$$isDone#";
    private static final String IS_COMPLETED_EXCEPTIONALLY = "$$isCompletedExceptionally#";
    private static final String IS_CANCELLED = "$$isCancelled#";

    private final RedissonClient redisson;
    private final RedissonCompletableTaskFutureHelper helper;

    RedissonCompletableFuture(RedissonClient redisson, RedissonCompletableTaskFutureHelper helper, ITaskIdentify taskContext) {
        super(taskContext);
        this.redisson = redisson;
        this.helper = helper;
    }

    /**
     * cf RedissonCompletableTaskFutureHelper for more detail
     * @return the result when task finishes
     */
    @Override
    public T join() {
        return helper.join(this);
    }

    /**
     * @return to check if task isDone
     */
    @Override
    public boolean isDone() {
        return !redisson.isShutdown() && getIsDoneSet().contains(getKey());
    }

    /**
     * cf RedissonCompletableTaskFutureHelper for more detail
     * to publish complete event in redis
     * @return successful or not
     */
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

    /**
     * cf RedissonCompletableTaskFutureHelper for more detail
     * to publish completeExceptionally event in redis
     * @return successful or not
     */
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

    /**
     * @return to check if task isCompletedExceptionally
     */
    @Override
    public boolean isCompletedExceptionally() {
        return !redisson.isShutdown() && getIsCompletedExceptionallySet().contains(getKey());
    }

    /**
     * @return to check if task isCancelled
     */
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
