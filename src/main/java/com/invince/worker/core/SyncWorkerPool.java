package com.invince.worker.core;

import lombok.extern.slf4j.Slf4j;

/**
 * Sync workerpool, you can wait until a group of tasks finish
 * We just define the task type here
 */
@Slf4j
public class SyncWorkerPool<T extends AbstractTask, GroupByType> extends AbstractSyncWorkerPool<T, GroupByType, Void> {

    public SyncWorkerPool(WorkerPoolSetup config) {
        super(config);
    }
}
