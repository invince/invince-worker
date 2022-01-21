package io.github.invince.worker.adapter.local.collections;

import io.github.invince.worker.core.collections.ITaskGroups;
import io.github.invince.worker.core.future.CompletableTaskFuture;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DefaultTaskGroups is a Map of group -> list of taskFuture (on CompletableTaskFuture you can find the task key, task prefix)
 *
 * @param <GroupByType> groupKey type
 * @param <SingleResult> result type for a single task
 */
public class DefaultTaskGroups<GroupByType, SingleResult> implements ITaskGroups<GroupByType, SingleResult> {

    protected final Map<GroupByType, ConcurrentLinkedQueue<CompletableTaskFuture<SingleResult>>>
            requestTaskMap = new ConcurrentHashMap<>();

    /**
     * Get or Create the group
     * @param groupKey group key
     * @return the queue of taskFuture of that group
     */
    @Override
    public Queue<CompletableTaskFuture<SingleResult>> getOrCreate(GroupByType groupKey) {
        requestTaskMap.putIfAbsent(groupKey, new ConcurrentLinkedQueue<>());
        return requestTaskMap.get(groupKey);
    }

    /**
     * Check if group exist and if we have already taskFuture in it
     * @param groupKey group key
     * @return if group exist and if we have already taskFuture in it
     */
    @Override
    public boolean existNotEmptyGroup(GroupByType groupKey) {
        return requestTaskMap.containsKey(groupKey) && !requestTaskMap.get(groupKey).isEmpty();
    }

    /**
     * remove the group
     * @param groupKey group key
     */
    @Override
    public void remove(GroupByType groupKey) {
        requestTaskMap.remove(groupKey);
    }
}
