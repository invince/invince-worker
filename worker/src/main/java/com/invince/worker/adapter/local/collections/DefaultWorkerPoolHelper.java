package com.invince.worker.adapter.local.collections;

import com.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import com.invince.worker.core.BaseTask;
import com.invince.worker.core.collections.IProcessingTasks;
import com.invince.worker.core.collections.ITaskGroups;
import com.invince.worker.core.collections.IToDoTasks;
import com.invince.worker.core.collections.IWorkerPoolHelper;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.springframework.stereotype.Service;

@Service
public class DefaultWorkerPoolHelper implements IWorkerPoolHelper {

    private final ICompletableTaskFutureService completableTaskFutureService = new DefaultCompletableTaskFutureService();

    @Override
    public IToDoTasks newToDoTasks(String name) {
        return new DefaultToDoTasks();
    }

    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name, String poolUid) {
        return new DefaultProcessingTasks<>();
    }

    @Override
    public <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String name) {
        return new DefaultTaskGroups<>();
    }

    @Override
    public ICompletableTaskFutureService getCompletableTaskFutureService() {
        return completableTaskFutureService;
    }
}
