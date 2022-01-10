package com.invince.worker.future;

import com.invince.worker.BaseTask;
import com.invince.worker.IWorkerPool;

import java.util.Collection;

public interface ISyncWorkerPool <T extends BaseTask<SingleResult>, GroupByType, SingleResult> extends IWorkerPool<T> {

    void enqueueAll(GroupByType groupName, Collection<T> tasks);

    void waitUntilFinish(GroupByType groupName);

    void cancelGroup(GroupByType groupName);
}
