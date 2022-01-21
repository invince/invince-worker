package com.invince.worker.adapter.local.helper;

import com.invince.worker.adapter.local.collections.DefaultProcessingTasks;
import com.invince.worker.adapter.local.collections.DefaultTaskGroups;
import com.invince.worker.adapter.local.collections.DefaultToDoTasks;
import com.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import com.invince.worker.core.BaseTask;
import com.invince.worker.core.collections.IProcessingTasks;
import com.invince.worker.core.collections.ITaskGroups;
import com.invince.worker.core.collections.IToDoTasks;
import com.invince.worker.core.helper.IWorkerPoolHelper;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.springframework.stereotype.Service;

/**
 * DefaultWorkerPoolHelper used to build your workerPool.
 */
@Service
public class DefaultWorkerPoolHelper implements IWorkerPoolHelper {

    private final ICompletableTaskFutureService completableTaskFutureService = new DefaultCompletableTaskFutureService();

    /**
     * Create a new DefaultToDoTasks
     * @param workerPoolName not useful in local mode
     * @return DefaultToDoTasks
     */
    @Override
    public IToDoTasks newToDoTasks(String workerPoolName) {
        return new DefaultToDoTasks();
    }

    /**
     * Create a new DefaultProcessingTasks
     * @param workerPoolName NA in local mode
     * @param poolUid NA in local mode
     * @param <T> the Task Type
     * @return DefaultProcessingTasks
     */
    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String workerPoolName, String poolUid) {
        return new DefaultProcessingTasks<>();
    }

    /**
     * Create a new DefaultTaskGroups, to map which task is in which group
     * @param workerPoolName NA in local mode
     * @param <GroupBy> group key type
     * @param <SingleResult> singleResult of a single task
     * @return DefaultTaskGroups
     */
    @Override
    public <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String workerPoolName) {
        return new DefaultTaskGroups<>();
    }

    /**
     * DefaultCompletableTaskFutureService to help generate CompletableTaskFuture from a baseTask
     * @return DefaultCompletableTaskFutureService
     */
    @Override
    public ICompletableTaskFutureService getCompletableTaskFutureService() {
        return completableTaskFutureService;
    }
}
