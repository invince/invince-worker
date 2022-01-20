package com.invince.worker.core.collections;

import com.invince.worker.core.future.CompletableTaskFuture;

import java.util.Queue;

public interface ITaskGroups <GroupByType, SingleResult> {

    Queue<CompletableTaskFuture<SingleResult>> getOrCreate(GroupByType group);

    boolean existNotEmptyGroup(GroupByType group);

    void remove(GroupByType group);

    default void close() {}
}
