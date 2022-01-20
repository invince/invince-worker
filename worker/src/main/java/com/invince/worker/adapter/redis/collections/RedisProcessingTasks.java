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

    @Override
    public V put(K key, V value) {
        getRedisMap().put(key, new ProcessingTaskWrapper<>(value, poolUid));
        tasksProcessingOnThisInstance.put(key, value);
        log.debug("Task {} move to redis processing map", value.getKey());
        return value;
    }

    @Override
    public V remove(Object key) {
        var wrapper = getRedisMap().remove(key);
        tasksProcessingOnThisInstance.remove(key);
        log.debug("Task {} removed from redis processing map", key);
        return wrapper.getTask();
    }

    @Override
    public boolean exist(K key) {
        return getRedisMap().containsKey(key);
    }

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
