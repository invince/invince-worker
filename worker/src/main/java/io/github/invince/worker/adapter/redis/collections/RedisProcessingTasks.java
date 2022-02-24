package io.github.invince.worker.adapter.redis.collections;

import io.github.invince.util.SafeRunner;
import io.github.invince.worker.adapter.local.collections.DefaultProcessingTasks;
import io.github.invince.worker.adapter.redis.collections.model.ProcessingTaskWrapper;
import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.collections.IProcessingTasks;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

/**
 * RedisProcessingTasks is a RMap of taskKey to ProcessingTaskWrapper
 *
 * @param <K> task key (we can use only task key, not the task uniqueKey, because each workerPool shall have its own IProcessingTasks)
 * @param <V> task type
 */
@Slf4j
public class RedisProcessingTasks<K, V extends BaseTask> implements IProcessingTasks<K, V> {

    private static final String ALIVE_FLAG = "$ALIVE_FLAG$";
    private static final String PROCESSING_LIST = "$PROCESSING_LIST$";
    private static final String PROCESSING_LIST_CRASH_SAFE = "$PROCESSING_LIST_CRASH_SAFE$";
    private static final String CANCEL_PROCESSING_TOPIC = "$CANCEL_PROCESSING_TOPIC$";

    private final RedissonClient redisson;

    private final String prefix;
    private final String poolUid;
    private final RLock aliveFlag; // RLock has a watchDog check, if app crashes, the lock will be released in next check

    private final DefaultProcessingTasks<K, V> tasksProcessingOnThisInstance = new DefaultProcessingTasks<>();


    /**
     *
     * @param redisson redisson
     * @param prefix the queue prefix
     * @param poolUid the uid of the pool, in distributed mode, pool on each node should have different poolUid
     */
    public RedisProcessingTasks(RedissonClient redisson, String prefix, String poolUid) {
        this.redisson = redisson;
        this.prefix = prefix;
        this.poolUid = poolUid;

        RTopic cancelProcessingTopic = redisson.getTopic(prefix + CANCEL_PROCESSING_TOPIC);
        // every node should listen to cancelProcessing topic.
        // means we can do task.cancel on one node, then redis publish that event and every node
        // check if the task is processing on itself, if yes, cancel it
        cancelProcessingTopic.addListener(String.class, (channel, keyToCancel) -> {
            if(keyToCancel == null) {
                log.warn("Null key to cancel");
                return;
            }
            RMap<K, ProcessingTaskWrapper<V>> map = getRedisProcessingMap();
            var wrapper = map.get(keyToCancel);
            if (wrapper != null && poolUid.equals(wrapper.getPoolProcessIt())) {
                cancelInLocal(keyToCancel);
            }
        });

        aliveFlag = getAliveFlagForPool(poolUid);
        aliveFlag.lock();
    }



    /**
     * Put a task into processingTasks.
     * We will put the task both on redis and tasksProcessingOnThisInstance
     *
     * @param key task key
     * @param task task
     * @return the added task
     */
    @Override
    public V put(K key, V task) {
        // put it into redis processing list
        getRedisProcessingMap().put(key, new ProcessingTaskWrapper<>(task, poolUid));
        // put it into redis crash safe list
        getRedisCrashSafeMap().put(key, task);

        // put it into local copy
        tasksProcessingOnThisInstance.put(key, task);
        log.debug("Task {} move to redis processing map", task.getKey());
        return task;
    }


    /**
     * Remove a task from processingTasks.
     * We will remove the task both on redis and tasksProcessingOnThisInstance
     *
     * @param key task key
     * @return the removed task
     */
    @Override
    public V remove(Object key) {
        // remove it from redis processing list
        var wrapper = getRedisProcessingMap().remove(key);
        // remove it from redis crash safe list
        getRedisCrashSafeMap().remove(key);
        // remove it from local copy
        tasksProcessingOnThisInstance.remove(key);
        log.debug("Task {} removed from redis processing map", key);
        return wrapper.getTask();
    }

    /**
     * Check if task key exist in processing list
     * @param key task key
     * @return if task key exist in processing list
     */
    @Override
    public boolean exist(K key) {
        var processingList = getRedisProcessingMap();
        var taskWrapper = processingList.get(key);
        if (taskWrapper != null) {
            if (getAliveFlagForPool(taskWrapper.getPoolProcessIt()).isLocked()) {
                // means the pool (of that worker node) is still alive
                return true;
            } else {
                // the pool process that task is dead
                // remove it from redis processing list
                getRedisProcessingMap().remove(key);
                // remove it from local copy
                tasksProcessingOnThisInstance.remove(key);
                return false;
            }
        }

        return false;
    }

    /**
     * Cancel a task via task key.
     * If task is processing on same node, cancel it, otherwise broadcast event in cancelProcessingTopic
     * @param key task key
     */
    @Override
    public void cancel(String key) {
        if (!StringUtils.hasText(key)) {
            RMap<K, ProcessingTaskWrapper<V>> map = getRedisProcessingMap();
            var wrapper = map.get(key);
            if (wrapper != null && poolUid.equals(wrapper.getPoolProcessIt())) {
                cancelInLocal(key);
            } else {
                log.debug("Task {} is not processing on this node, we'll broadcast the cancel event to others", key);
                RTopic cancelProcessingTopic = redisson.getTopic(prefix + CANCEL_PROCESSING_TOPIC);
                cancelProcessingTopic.publish(key);
            }
        }
    }

    /**
     * @return size of the processing tasks
     */
    @Override
    public int size() {
        return getRedisProcessingMap().size();
    }

    /**
     * (In distributed mode), if your task is processed by a worker node, and that node crashes,
     * we shall be able to restore it and put it back to todo list
     * @param key task key
     * @param consumer consumer to rescue the task
     * @return success or not
     */
    @Override
    public boolean tryRestoreCrashedProcessingTask(K key, Consumer<V> consumer) {
       if (key == null || consumer == null) {
           return false;
       }
       var crashSafe = getRedisCrashSafeMap();
       V task= crashSafe.get(key);
       if (task != null && task.getRetryChances() > 0) {
           log.debug("Task {} is restored", key);
           task.setRetryChances(task.getRetryChances() - 1);
           SafeRunner.run(task::onRollbackBeforeRetry);
           consumer.accept(task);
           crashSafe.remove(key);
           return true;
       }
        return false;
    }

    /**
     * close the processingTasks collection if necessary
     */
    @Override
    public void close() {
        if (aliveFlag != null) {
            aliveFlag.unlock();
        }
    }

    private void cancelInLocal(String keyToCancel) {
        V task = tasksProcessingOnThisInstance.get(keyToCancel);
        if(task != null) {
            log.debug("Task {} is processing on this node, we'll cancel it", keyToCancel);
            SafeRunner.run(task::cancelProcessing);
        } else {
            log.warn("Task {} not found in local processing copy", keyToCancel);
        }
    }

    private RMap<K, ProcessingTaskWrapper<V>> getRedisProcessingMap() {
        return redisson.getMap(prefix + PROCESSING_LIST);
    }

    private RMap<K, V> getRedisCrashSafeMap() {
        return redisson.getMap(prefix + PROCESSING_LIST_CRASH_SAFE);
    }

    private RLock getAliveFlagForPool(String poolUid) {
        return redisson.getLock(prefix + ALIVE_FLAG + poolUid);
    }
}
