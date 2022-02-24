package io.github.invince.worker.core.collections;

import io.github.invince.worker.core.BaseTask;

import java.util.function.Consumer;

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

    /**
     * (In distributed mode), if your task is processed by a worker node, and that node crashes,
     * we shall be able to restore it and put it back to todo list
     * @param key task key
     * @param consumer consumer to rescue the task
     * @return success or not
     */
    boolean tryRestoreCrashedProcessingTask(K key, Consumer<V> consumer);
}
