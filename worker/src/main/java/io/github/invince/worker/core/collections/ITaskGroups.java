package io.github.invince.worker.core.collections;

import io.github.invince.worker.core.future.CompletableTaskFuture;

import java.util.Queue;


public interface ITaskGroups <GroupByType, SingleResult> {

    /**
     * Get or Create the queue of taskFuture for that group
     * @param group group
     * @return the queue of taskFuture of that group
     */
    Queue<CompletableTaskFuture<SingleResult>> getOrCreate(GroupByType group);

    /**
     * Check if group exist and if we have already taskFuture in it
     * @param group group key
     * @return if group exist and if we have already taskFuture in it
     */
    boolean existNotEmptyGroup(GroupByType group);

    /**
     * remove the group
     * @param group group key
     */
    void remove(GroupByType group);

    /**
     * close the ITaskGroups collection if necessary
     */
    default void close() {}
}
