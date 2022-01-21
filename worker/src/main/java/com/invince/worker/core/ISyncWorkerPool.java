package com.invince.worker.core;

import java.util.Collection;

/**
 * in additional of IWorkerPool, you can group your task when you enqueue them, and waitUntilFinish for all tasks in that group
 * @param <T> task type
 * @param <GroupByType> group key type
 * @param <SingleResult> SingleResult of a single task
 */
public interface ISyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult> extends IWorkerPool<T> {

    /**
     * Enqueue tasks in a group, you can call this separately, all task in same group counts
     * @param groupName groupName
     * @param tasks tasks
     */
    void enqueueAll(GroupByType groupName, Collection<T> tasks);

    /**
     * wait all task in same group finishes
     * @param groupName groupName
     */
    void waitUntilFinish(GroupByType groupName);

    /**
     * cancel all tasks in same group
     * @param groupName groupName
     */
    void cancelGroup(GroupByType groupName);
}
