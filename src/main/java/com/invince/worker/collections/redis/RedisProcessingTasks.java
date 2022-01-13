package com.invince.worker.collections.redis;

import com.invince.util.SafeRunner;
import com.invince.worker.BaseTask;
import com.invince.worker.collections.IProcessingTasks;
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


    public RedisProcessingTasks(RedissonClient redisson, String prefix, String poolUid) {
        this.redisson = redisson;
        this.prefix = prefix;
        this.poolUid = poolUid;

        RTopic cancelProcessingTopic = redisson.getTopic(prefix + CANCEL_PROCESSING_TOPIC);
        cancelProcessingTopic.addListener(String.class, (channel, keyToCancel) -> {
            RMap<K, TaskInProcessingWrapper<V>> map = getRedisMap();
            var wrapper = map.get(keyToCancel);
            if (wrapper != null && poolUid.equals(wrapper.getPoolProcessIt())) {
                log.info("Task {} is processing on this node, we'll cancel it", keyToCancel);
                SafeRunner.run(() -> wrapper.getTask().cancelProcessing());
            }
        });
    }

    @Override
    public V put(K key, V value) {
        getRedisMap().put(key, new TaskInProcessingWrapper<>(value, poolUid));
        log.debug("Task {} move to redis processing map", value.getKey());
        return value;
    }

    @Override
    public V remove(Object key) {
        var wrapper = getRedisMap().remove(key);
        log.debug("Task {} removed from redis processing map", key);
        return wrapper.getTask();
    }

    @Override
    public boolean exist(K key) {
        return getRedisMap().containsKey(key);
    }

    @Override
    public void cancel(K key) {
        if (!StringUtils.isEmpty(key)) {
            RMap<K, TaskInProcessingWrapper<V>> map = getRedisMap();
            var wrapper = map.get(key);
            if (wrapper != null && poolUid.equals(wrapper.getPoolProcessIt())) {
                log.info("Task {} is processing on this node, we'll cancel it", key);
                SafeRunner.run(() -> wrapper.getTask().cancelProcessing());
            } else {
                log.info("Task {} is not processing on this node, we'll broadcast the cancel event to others", key);
                RTopic cancelProcessingTopic = redisson.getTopic(prefix + CANCEL_PROCESSING_TOPIC);
                cancelProcessingTopic.publish(key);
            }
        }
    }

    @Override
    public int size() {
        return getRedisMap().size();
    }

    private RMap<K, TaskInProcessingWrapper<V>> getRedisMap() {
        return redisson.getMap(prefix + PROCESSING_LIST);
    }
}
