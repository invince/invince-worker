package io.github.invince.worker.adapter.redis.future;

import io.github.invince.exception.WorkerError;
import io.github.invince.exception.WorkerRemoteError;
import io.github.invince.util.EnhancedJsonJacksonCodec;
import io.github.invince.worker.core.ITaskIdentify;
import io.github.invince.worker.core.future.CompletableTaskFuture;
import io.github.invince.spring.WorkerPoolConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper to simulate join, complete, completeExceptionally... in redis mode
 */
@Slf4j
@Primary
@Service
@Profile(WorkerPoolConfiguration.PROFILE_REDIS)
public class RedissonCompletableTaskFutureHelper {

    private static final String GLOBAL_FINISH_TOPIC = "%%GLOBAL_FINISH_TOPIC%%";

    private final RedissonClient redisson;

    // We will keep a map of task -> CompletableTaskFuture, if one CompletableTaskFuture does join/wait (means it's waiting for the result), we put it into this map
    private final Map<String, CompletableTaskFuture> waitedTask = new ConcurrentHashMap<>();
    private final Map<String, RedissonCompletableFutureResultHolder> notWaitedTask = new ConcurrentHashMap<>();


    @Autowired
    public RedissonCompletableTaskFutureHelper(RedissonClient redisson) {
        this.redisson = redisson;
        // we use JsonJacksonCodec instead of jboss MarshallingCodec (the default one),
        // sometimes the Marshalling one has pb on classloader, the json one is more flexible
        listenToFinishTopic();

    }

    /**
     * We will listen to the finishTopic, once receive something:
     * - we check if it's waited by this node, if yes, we find the CompletableTaskFuture from waitedTask (cf comments on waitedTask and join) and call complete/completeExceptionally
     * - if it's not waited by this node, we keep the result in notWaitedTask, maybe later we will do join/wait on it
     */
    private void listenToFinishTopic() {
        RTopic finishTopic = getFinishTopic();
        finishTopic.addListener(RedissonCompletableFutureResultHolder.class, (channel, resultHolder) -> {
            String uniqueKeyFinished = resultHolder.getUniqueKey();
            log.debug("Finish {} event received", uniqueKeyFinished);
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

    /**
     * we'll create a unique CompletableTaskFuture for each context, if you do join, we put it into waitedTask
     * and wait the redis finishTopic receive the finishEvent of that task
     *
     * @param context task context
     * @param <T> result type
     * @return result
     */
    public <T> T join(ITaskIdentify context) {
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

    /**
     * If the task is completeExceptionally on the working node, we shall publish that finishEvent with exception in finishTopic
     * @param context task context
     * @param ex the exception
     * @return successful or not
     */
    public boolean completeExceptionally(ITaskIdentify context, Throwable ex) {
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

    /**
     * If the task is complete with a result on the working node, we shall publish that finishEvent with result in finishTopic
     * @param context task context
     * @param value the result
     * @return successful or not
     */
    public <T> boolean complete(ITaskIdentify context, T value) {
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
