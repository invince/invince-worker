package io.github.invince.worker.core;

import lombok.extern.slf4j.Slf4j;

/**
 * in additional of IWorkerPool, you can group your task when you enqueue them, and waitUntilFinish for all tasks in that group
 * We just define the task type and Void SingleResultType here, details cf AbstractSyncWorkerPool
 *
 * @param <T> task type
 * @param <GroupByType> group key type
 */
@Slf4j
public class SyncWorkerPool<T extends AbstractTask, GroupByType> extends AbstractSyncWorkerPool<T, GroupByType, Void> {

    public SyncWorkerPool(WorkerPoolSetup config) {
        super(config);
    }
}
