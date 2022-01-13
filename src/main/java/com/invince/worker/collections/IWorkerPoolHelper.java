package com.invince.worker.collections;

import com.invince.worker.BaseTask;

public interface IWorkerPoolHelper {

    IToDoTasks newToDoTasks(String name);

    // in distributed mode, you can know who (which workerpool) takes the task
    <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name, String poolUid);

    <GroupBy, T extends BaseTask> ITaskGroups<GroupBy, T> newTaskGroups(String name);
}
