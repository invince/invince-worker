package com.invince.worker.collections;

import com.invince.worker.BaseTask;

import java.util.Queue;

public interface ITaskGroups <GroupByType, T extends BaseTask> {

    Queue<T> getOrCreate(GroupByType group);

    boolean existNotEmptyGroup(GroupByType group);

    Queue<T> remove(GroupByType group);
}
