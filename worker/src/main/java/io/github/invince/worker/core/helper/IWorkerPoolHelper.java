package io.github.invince.worker.core.helper;

import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.ITaskGroups;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;

/**
 * helper used to build your workerPool.
 *
 * You can define your toDo, processing and taskGroup collections, and also how to create a CompletableTaskFuture from the task.
 * For ex: you could have (we provided) a redis version of toDo list,
 *         so that, you can have one app put task into Redis Queue, and another app take and process it.
 *         In syncWorkPool, the 1st app can also receive taskFinish event from the 2nd app
 *
 */
public interface IWorkerPoolHelper {

    /**
     * @param workerPoolName workerPoolName
     * @return create a blocking queue type tado list
     */
    IToDoTasks newToDoTasks(String workerPoolName);

    /**
     *
     * @param workerPoolName workerPoolName
     * @param poolUid in distributed mode, you can know who (which workerpool) takes the task
     * @param <T> task type
     * @return Create a new processing task list
     */
    <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String workerPoolName, String poolUid);

    /**
     * @param workerPoolName workerPoolName
     * @param <GroupBy> group key type
     * @param <SingleResult> singleResult of a single task
     * @return a map of group and all tasks in same group
     */
    <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String workerPoolName) ;

    /**
     * @return ICompletableTaskFutureService to help generate CompletableTaskFuture from a baseTask
     */
    ICompletableTaskFutureService getCompletableTaskFutureService();
}
