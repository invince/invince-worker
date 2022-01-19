package com.invince.worker.core.collections;

import com.invince.worker.core.BaseTask;
import com.invince.worker.core.future.ICompletableTaskFutureService;

public interface IWorkerPoolHelper {

    IToDoTasks newToDoTasks(String name);

    // in distributed mode, you can know who (which workerpool) takes the task
    <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name, String poolUid);

    <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String name) ;

    ICompletableTaskFutureService getCompletableTaskFutureService();
}
