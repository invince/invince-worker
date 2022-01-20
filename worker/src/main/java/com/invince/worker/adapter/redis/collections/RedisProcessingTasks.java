package com.invince.worker.adapter.redis.collections;

import com.invince.util.SafeRunner;
import com.invince.worker.adapter.local.collections.DefaultProcessingTasks;
import com.invince.worker.adapter.redis.collections.model.ProcessingTaskWrapper;
import com.invince.worker.core.BaseTask;
import com.invince.worker.core.collections.IProcessingTasks;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

/**
 * RedisProcessingTasks is a RMap of taskKey -> ProcessingTaskWrapper
 *
 * @param <K> task key (we can use only task key, not the task uniqueKey, because each workerPool shall have its own IProcessingTasks)
 * @param <V> task type
 */
@Slf4j
public class RedisProcessingTasks<K, V extends BaseTask> implements IProcessingTasks<K, V> {

    private static final String PROCESSING_LIST = "$PROCESSING_LIST$";
    private static final String CANCEL_PROCESSING_TOPIC = "$CANCEL_PROCESSING_TOPIC$";

    private final RedissonClient redisson;

    private final String prefix;
    private final String poolUid;

    private final DefaultProcessingTasks<K, V> tasksProcessingOnThisInstance = new DefaultProcessingTasks<>();


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
            RMap<K, ProcessingTaskWrapper<V>> map = getRedisMap();
            var wrapper = map.get(keyToCancel);
            if (wrapper != null && poolUid.equals(wrapper.getPoolProcessIt())) {
                cancelInLocal(keyToCancel);
            }
        });
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
        getRedisMap().put(key, new ProcessingTaskWrapper<>(task, poolUid));
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
        var wrapper = getRedisMap().remove(key);
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
        return getRedisMap().containsKey(key);
    }

    /**
     * Cancel a task via task key.
     * If task is processing on same node, cancel it, otherwise broadcast event in cancelProcessingTopic
     * @param key task key
     */
    @Override
    public void cancel(String key) {
        if (!StringUtils.isEmpty(key)) {
            RMap<K, ProcessingTaskWrapper<V>> map = getRedisMap();
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
        return getRedisMap().size();
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

    private RMap<K, ProcessingTaskWrapper<V>> getRedisMap() {
        return redisson.getMap(prefix + PROCESSING_LIST);
    }
}
