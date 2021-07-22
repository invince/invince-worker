package com.invince.worker.collections.local;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.ITaskGroups;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultTaskGroups<GroupBy, T extends BaseTask> implements ITaskGroups<GroupBy, T> {

    protected final Map<GroupBy, ConcurrentLinkedQueue<T>> requestTaskMap = new ConcurrentHashMap<>();

    @Override
    public Queue<T> getOrCreate(GroupBy groupBy) {
        requestTaskMap.putIfAbsent(groupBy, new ConcurrentLinkedQueue<>());
        return requestTaskMap.get(groupBy);
    }

    @Override
    public boolean existNotEmptyGroup(GroupBy groupBy) {
        return requestTaskMap.containsKey(groupBy) && !requestTaskMap.get(groupBy).isEmpty();
    }

    @Override
    public Queue<T> remove(GroupBy groupBy) {
        return requestTaskMap.remove(groupBy);
    }
}
