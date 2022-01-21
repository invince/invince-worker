package io.github.invince.worker.core.collections;

import io.github.invince.worker.core.BaseTask;

public interface IToDoTasks {

    /**
     * Start listen to the todo list, if nbWorker = 0, we don't start it
     */
    default void startListening() {}

    /**
     *
     * @return size of the todo list
     */
    int size();

    /**
     * Check if task key exist in todo list
     * @param key task key
     * @return if task key exist in todo list
     */
    boolean exist(String key);

    /**
     * Task a task
     * @return the task
     * @throws InterruptedException
     */
    BaseTask take() throws InterruptedException;

    /**
     * Add new task in toDo list
     * @param task the task
     * @return successful or not
     */
    boolean add(BaseTask task);


    /**
     * the task is moved to processing, do cleaning on todo list if necessary
     * @param key task key
     * @return successful or not
     */
    boolean movedToProcessing(String key);

    /**
     * Cancel a task in todo list via task key.
     * @param key task key
     */
    void cancel(String key);

    /**
     * close the todo list collection if necessary
     */
    default void close() {}
}
