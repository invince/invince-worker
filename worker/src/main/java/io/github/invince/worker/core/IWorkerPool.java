package io.github.invince.worker.core;

/**
 * Blocking queue style workerPool
 * NOTE: Do not forget to call shutdown when workerPool ends
 * @param <T> task type
 */
public interface IWorkerPool<T extends BaseTask> {

    /**
     *
     * @return name of the workerPool
     */
    default String getName() {
        return this.getClass().getName();
    }

    /**
     *
     * @return in which group this workerPool is, by default each one is in its own group, used in monitor service
     */
    default String getGroupName() {
        return this.getClass().getName();
    }

    /**
     *
     * @return todo list size, used in monitor service
     */
    int getToDoListSize();

    /**
     *
     * @return processing list size, used in monitor service
     */
    int getProcessingListSize();

    /**
     *
     * @return nb of permanent worker started, used in monitor service
     */
    int getPermanentWorkerSize();

    /**
     * Enqueue a task in the workpool
     * @param task task
     * @return the task
     */
    T enqueue(T task);

    /**
     *
     * @param key task key
     * @return if task exists in todo list
     */
    boolean existToDoTask(String key);

    /**
     *
     * @param key task key
     * @return if task exists in processing list
     */
    boolean existProcessingTask(String key);

    /**
     * remove the task via
     * @param key task key
     * @return the removed task
     */
    T removeTask(String key);

    /**
     * shutdown the workpool
     * @param await if we wait current process finish
     */
    void shutdown(boolean await);

    /**
     * cancel a task (no matter it's in todo list or processing list)
     * @param key task key
     */
    void cancelTask(String key);


    /**
     * (In distributed mode), if your task is processed by a worker node, and that node crashes,
     * we shall be able to restore it and put it back to todo list
     * @param key task key
     * @return success or not
     */
    boolean tryRestoreCrashedProcessingTask(String key);
}
