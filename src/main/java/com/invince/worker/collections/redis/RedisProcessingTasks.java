package com.invince.worker.collections.redis;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IProcessingTasks;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

@Slf4j
public class RedisProcessingTasks<K, V extends BaseTask> implements IProcessingTasks<K, V> {

    private static final String PROCESSING_LIST = "$PROCESSING_LIST$";

    private final RedissonClient redisson;

    private final String prefix;


    public RedisProcessingTasks(RedissonClient redisson, String prefix) {
        this.redisson = redisson;
        this.prefix = prefix;
    }

    @Override
    public V put(K key, V value) {
        getRedisMap().put(key, value);
        log.debug("Task {} move to redis processing map", value.getKey());
        return value;
    }

    @Override
    public V remove(Object key) {
        V task = getRedisMap().remove(key);
        log.debug("Task {} removed from redis processing map", task.getKey());
        return task;
    }

    @Override
    public boolean exist(K key) {
        return getRedisMap().containsKey(key);
    }

    @Override
    public void cancel(K key) {
        if (!StringUtils.isEmpty(key)) {
            var task = getRedisMap().get(key);
            if (task != null) {
                task.cancelProcessing();
            }
        }
    }

    @Override
    public int size() {
        return getRedisMap().size();
    }

    private RMap<K, V> getRedisMap() {
        return redisson.getMap(prefix + PROCESSING_LIST);
    }
}
