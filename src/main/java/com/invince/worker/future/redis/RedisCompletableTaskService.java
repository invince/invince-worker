package com.invince.worker.future.redis;

import com.invince.worker.BaseTask;
import com.invince.worker.future.ICompletableTaskService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.CompletableFuture;

import static com.invince.spring.WorkerConfig.PROFILE_REDIS;


@Slf4j
@Primary
@Profile(PROFILE_REDIS)
public class RedisCompletableTaskService implements ICompletableTaskService {

    private final RedissonClient redisson;

    @Autowired
    public RedisCompletableTaskService(RedissonClient redisson) {
        this.redisson = redisson;
    }


    @Override
    public <T> CompletableFuture<T> getOrWrap(BaseTask tBaseTask) {
        return new RedissonCompletableFuture<>(redisson, tBaseTask);
    }

    @Override
    public void release(BaseTask task) {
        log.debug("Nothing to do in redis mode");
    }
}
