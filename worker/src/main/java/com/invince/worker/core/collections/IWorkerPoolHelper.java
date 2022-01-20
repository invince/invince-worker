package com.invince.worker.core.collections;

import com.invince.worker.core.BaseTask;
import com.invince.worker.core.future.ICompletableTaskFutureService;

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

    IToDoTasks newToDoTasks(String name);

    // in distributed mode, you can know who (which workerpool) takes the task
    <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name, String poolUid);

    <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String name) ;

    ICompletableTaskFutureService getCompletableTaskFutureService();
}
