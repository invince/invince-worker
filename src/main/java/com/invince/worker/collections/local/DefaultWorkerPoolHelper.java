package com.invince.worker.collections.local;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IProcessingTasks;
import com.invince.worker.collections.ITaskGroups;
import com.invince.worker.collections.IToDoTasks;
import com.invince.worker.collections.IWorkerPoolHelper;
import org.springframework.stereotype.Service;

@Service
public class DefaultWorkerPoolHelper implements IWorkerPoolHelper {
    @Override
    public IToDoTasks newToDoTasks(String name) {
        return new DefaultToDoTasks();
    }

    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name) {
        return new DefaultProcessingTasks<>();
    }

    @Override
    public <GroupBy, T extends BaseTask> ITaskGroups<GroupBy, T> newTaskGroups(String name) {
        return new DefaultTaskGroups<>();
    }
}
