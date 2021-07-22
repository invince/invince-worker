package com.invince.worker.collections;

import com.invince.worker.BaseTask;

public interface IWorkerPoolHelper {

    IToDoTasks newToDoTasks(String name);

    <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name);

    <GroupBy, T extends BaseTask> ITaskGroups<GroupBy, T> newTaskGroups(String name);
}
