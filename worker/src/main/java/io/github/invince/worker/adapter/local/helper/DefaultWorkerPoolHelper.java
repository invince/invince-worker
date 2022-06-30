package io.github.invince.worker.adapter.local.helper;

import io.github.invince.worker.adapter.local.collections.DefaultProcessingTasks;
import io.github.invince.worker.adapter.local.collections.DefaultTaskGroups;
import io.github.invince.worker.adapter.local.collections.DefaultToDoTasks;
import io.github.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.WorkerController;
import io.github.invince.worker.core.WorkerPoolSetup;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.ITaskGroups;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.core.helper.IWorkerPoolHelper;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;
import org.springframework.stereotype.Service;

/**
 * DefaultWorkerPoolHelper used to build your workerPool.
 */
@Service
public class DefaultWorkerPoolHelper implements IWorkerPoolHelper {

    private final ICompletableTaskFutureService completableTaskFutureService = new DefaultCompletableTaskFutureService();

    /**
     * Create a new DefaultToDoTasks
     *
     * @param setup NA in local mode
     * @param workerController NA in local mode
     * @param poolUid NA in local mode
     * @return DefaultToDoTasks
     */
    @Override
    public <T extends BaseTask> IToDoTasks newToDoTasks(WorkerPoolSetup setup, WorkerController<T> workerController, String poolUid) {
        return new DefaultToDoTasks();
    }

    /**
     * Create a new DefaultProcessingTasks
     * @param setup NA in local mode
     * @param workerController NA in local mode
     * @param poolUid NA in local mode
     * @param <T> the Task Type
     * @return DefaultProcessingTasks
     */
    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(WorkerPoolSetup setup, WorkerController<T> workerController, String poolUid) {
        return new DefaultProcessingTasks<>();
    }

    /**
     * Create a new DefaultTaskGroups, to map which task is in which group
     * 
     * @param setup NA in local mode
     * @param workerController workerController
     * @param <GroupBy> group key type
     * @param <SingleResult> singleResult of a single task
     * @return DefaultTaskGroups
     */
    @Override
    public <T extends BaseTask, GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(WorkerPoolSetup setup, WorkerController<T> workerController) {
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
