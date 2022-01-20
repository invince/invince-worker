package com.invince.worker.adapter.local.collections;

import com.invince.worker.core.collections.ITaskGroups;
import com.invince.worker.core.future.CompletableTaskFuture;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DefaultTaskGroups is a Map of group -> list of taskFuture (on CompletableTaskFuture you can find the task key, task prefix)
 *
 * @param <GroupBy> group
 * @param <SingleResult> result type for a single task
 */
public class DefaultTaskGroups<GroupBy, SingleResult> implements ITaskGroups<GroupBy, SingleResult> {

    protected final Map<GroupBy, ConcurrentLinkedQueue<CompletableTaskFuture<SingleResult>>>
            requestTaskMap = new ConcurrentHashMap<>();

    @Override
    public Queue<CompletableTaskFuture<SingleResult>> getOrCreate(GroupBy groupBy) {
        requestTaskMap.putIfAbsent(groupBy, new ConcurrentLinkedQueue<>());
        return requestTaskMap.get(groupBy);
    }

    @Override
    public boolean existNotEmptyGroup(GroupBy groupBy) {
        return requestTaskMap.containsKey(groupBy) && !requestTaskMap.get(groupBy).isEmpty();
    }

    @Override
    public void remove(GroupBy groupBy) {
        requestTaskMap.remove(groupBy);
    }
}
