package com.invince.worker.core.collections;

import com.invince.worker.core.BaseTask;

public interface IProcessingTasks<K, V extends BaseTask> {

    /**
     * Put a task into processingTasks.
     *
     * @param key task key
     * @param task task
     * @return the added task
     */
    V put(K key, V task);

    /**
     * Remove a task from processingTasks.
     *
     * @param key task key
     * @return the removed task
     */
    V remove(Object key);

    /**
     * Check if task key exist in processing list
     * @param key task key
     * @return if task key exist in processing list
     */
    boolean exist(K key);

    /**
     * @return size of the processing tasks
     */
    int size();

    /**
     * Cancel a task in processing list via task key.
     * @param key task key
     */
    void cancel(String key);

    /**
     * close the processingTasks collection if necessary
     */
    default void close() {}
}
