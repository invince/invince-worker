package com.invince.worker.core;

public interface IWorkerPool<T extends BaseTask> {

    default String getName() {
        return this.getClass().getName();
    }

    default String getGroupName() {
        return this.getClass().getName();
    }

    int getToDoListSize();

    int getProcessingListSize();

    int getPermanentWorkerSize();

    T enqueue(T task);

    boolean existToDoTask(String key);

    boolean existProcessingTask(String key);

    T removeTask(String key);

    void shutdown(boolean await);

    void cancelTask(String key);
}
