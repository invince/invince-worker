package com.invince.worker.collections.redis;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.ITaskGroups;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.util.Queue;

import static org.springframework.util.CollectionUtils.isEmpty;

public class RedisTaskGroups<GroupByType, T extends BaseTask> implements ITaskGroups<GroupByType, T> {

    private static final String TASK_GROUPS = "$TASK_GROUPS$:::";

    private final RedissonClient redisson;

    private final String prefix;

    public RedisTaskGroups(RedissonClient redisson, String prefix) {
        this.redisson = redisson;
        this.prefix = prefix;
    }

    @Override
    public Queue<T> getOrCreate(GroupByType group) {
        return getRedisQueue(group);
    }

    @Override
    public boolean existNotEmptyGroup(GroupByType group) {
        return isEmpty(getRedisQueue(group));
    }

    @Override
    public Queue<T> remove(GroupByType group) {
        var queue = getRedisQueue(group);
        queue.clear();
        return queue;
    }


    private RQueue<T> getRedisQueue(GroupByType group) {
        return redisson.getQueue(prefix + TASK_GROUPS + group);
    }
}
