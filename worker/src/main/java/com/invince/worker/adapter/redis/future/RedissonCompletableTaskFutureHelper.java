package com.invince.worker.adapter.redis.future;

import com.invince.exception.WorkerError;
import com.invince.exception.WorkerRemoteError;
import com.invince.util.EnhancedJsonJacksonCodec;
import com.invince.worker.core.ITaskContext;
import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.invince.spring.WorkerConfig.PROFILE_REDIS;


@Slf4j
@Primary
@Profile(PROFILE_REDIS)
public class RedissonCompletableTaskFutureHelper {

    private static final String GLOBAL_FINISH_TOPIC = "%%GLOBAL_FINISH_TOPIC%%";

    private final RedissonClient redisson;

    private final Map<String, CompletableTaskFuture> waitedTask = new ConcurrentHashMap<>();
    private final Map<String, RedissonCompletableFutureResultHolder> notWaitedTask = new ConcurrentHashMap<>();


    @Autowired
    public RedissonCompletableTaskFutureHelper(RedissonClient redisson) {
        this.redisson = redisson;
        // we use JsonJacksonCodec instead of jboss MarshallingCodec (the default one),
        // sometimes the Marshalling one has pb on classloader, the json one is more flexible
        RTopic finishTopic = getFinishTopic();
        finishTopic.addListener(RedissonCompletableFutureResultHolder.class, (channel, resultHolder) -> {
            String uniqueKeyFinished = resultHolder.getUniqueKey();
            if (waitedTask.containsKey(uniqueKeyFinished)) {
                var future = waitedTask.get(uniqueKeyFinished);
                if (future == null) {
                    // something is wrong, waitedTask contains null future
                    notWaitedTask.put(uniqueKeyFinished, resultHolder);
                } else {
                    if (resultHolder.exception != null) {
                        future.completeExceptionally(new WorkerError(resultHolder.exception.getMessage(), resultHolder.exception));
                    } else {
                        future.complete(resultHolder.result);
                    }
                }
                waitedTask.remove(uniqueKeyFinished); // maybe remove 2 times, but it's ok
            } else {
                notWaitedTask.put(uniqueKeyFinished, resultHolder);
            }
        });
    }

    public <T> T join(ITaskContext context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        if(notWaitedTask.containsKey(context.getUniqueKey())) {
            var resultHolder = notWaitedTask.remove(context.getUniqueKey());
            if (resultHolder.exception != null) {
                throw new WorkerError(resultHolder.exception.getMessage(), resultHolder.exception);
            } else {
                return (T) resultHolder.result;
            }
        } else {
            CompletableTaskFuture future = new CompletableTaskFuture(context);
            try {
                waitedTask.put(context.getUniqueKey(), future);
                return (T) future.join();
            } finally {
                waitedTask.remove(context.getUniqueKey()); // maybe remove 2 times, but it's ok
            }
        }
    }

    public boolean completeExceptionally(ITaskContext context, Throwable ex) {
        WorkerError.verify("Null context to copy")
                .nonNull(context, ex)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        getFinishTopic().publish(
                new RedissonCompletableFutureResultHolder<>(context)
                        .setException(new WorkerRemoteError(ex.getMessage())));
        log.warn("Broadcast task completeExceptionally event for {}", context.getKey());
        return true;
    }

    public <T> boolean complete(ITaskContext context, T value) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        getFinishTopic().publish(
                new RedissonCompletableFutureResultHolder<>(context)
                    .setResult(value));
        log.warn("Broadcast task complete event for {}", context.getKey());
        return true;
    }


    private RTopic getFinishTopic() {
        return redisson.getTopic(GLOBAL_FINISH_TOPIC, EnhancedJsonJacksonCodec.get());
    }
}
