package com.invince.worker.adapter.redis.future;

import com.invince.worker.core.ITaskContext;
import com.invince.worker.core.future.CompletableTaskFuture;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static com.invince.spring.WorkerConfig.PROFILE_REDIS;


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


    @Override
    public <SingleResult> CompletableTaskFuture<SingleResult> getOrWrap(ITaskContext context) {
        return new RedissonCompletableFuture<>(redisson, helper, context);
    }

    @Override
    public void release(ITaskContext task) {
        log.debug("Nothing to do in redis mode");
    }
}
