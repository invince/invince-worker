package io.github.invince.worker.adapter.redis.future;

import io.github.invince.worker.core.ITaskIdentify;
import io.github.invince.worker.core.future.CompletableTaskFuture;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static io.github.invince.spring.WorkerPoolConfiguration.PROFILE_REDIS;

/**
 * RedisCompletableTaskFutureService to help generate/simulate distribute CompletableTaskFuture from a baseTask
 */
@Slf4j
@Primary
@Profile(PROFILE_REDIS)
public class RedisCompletableTaskFutureService implements ICompletableTaskFutureService {

    private final RedissonClient redisson;
    private final RedissonCompletableTaskFutureHelper helper;

    @Autowired
    public RedisCompletableTaskFutureService(RedissonClient redisson, RedissonCompletableTaskFutureHelper helper) {
        this.redisson = redisson;
        this.helper = helper;
    }

    /**
     * create a new RedissonCompletableFuture on that context, note for same context,
     * we shall send and receive same event for task complete, completeExceptionally ...
     *
     * @param context task key + task prefix
     * @param <SingleResult> resultType
     * @return CompletableTaskFuture
     */
    @Override
    public <SingleResult> CompletableTaskFuture<SingleResult> getOrWrap(ITaskIdentify context) {
        return new RedissonCompletableFuture<>(redisson, helper, context);
    }

    /**
     * Nothing to do on redis mode, we are based on event
     * @param task NA
     */
    @Override
    public void release(ITaskIdentify task) {
        log.debug("Nothing to do in redis mode");
    }
}
