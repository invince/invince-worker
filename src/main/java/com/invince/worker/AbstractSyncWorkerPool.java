package com.invince.worker;

import com.invince.spring.ContextHolder;
import com.invince.util.SafeRunner;
import com.invince.worker.collections.ITaskGroups;
import com.invince.worker.future.ICompletableTaskService;
import com.invince.worker.future.ISyncWorkerPool;
import com.invince.worker.future.local.DefaultCompletableTaskService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;

@Slf4j
class AbstractSyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult>
        extends StandardWorkerPool<T> implements ISyncWorkerPool<T, GroupByType, SingleResult> {

    protected ITaskGroups<GroupByType, T> requestTaskMap;

    public AbstractSyncWorkerPool(WorkerPoolSetup config) {
        super(config);
    }

    @Override
    void init() {
        // init before newWorker
        requestTaskMap = config.getHelper().newTaskGroups(config.getName());
        super.init();
    }

    @Override
    public void enqueueAll(GroupByType group, Collection<T> tasks) {
        if (tasks != null) {
            tasks.stream().filter(Objects::nonNull).forEach(one -> {
                enqueue(one);// this put the task in the blocking queue
                addIntoGroup(group, one); // this put task in the group, so we can wait result for a group
            });
        }
    }

    final void addIntoGroup(GroupByType group, T task) {
        if (task == null) {
            return;
        }
        requestTaskMap.getOrCreate(group).add(task);
    }

    @Override
    public void cancelGroup(GroupByType group) {
        if (requestTaskMap.existNotEmptyGroup(group)) {
            requestTaskMap.getOrCreate(group).forEach(task -> {
                var key = task.getKey();
                if (key != null) {
                    if (toDo.exist(key)) {
                        toDo.cancel(key);
                    } else if (processingTasks.exist(key)) {
                        processingTasks.cancel(key);
                    }
                }
            });
        }
    }

    @Override
    public final void waitUntilFinish(GroupByType group) {
        if (requestTaskMap.existNotEmptyGroup(group)) {
            // the custom RedissonCompletableFuture is not working well with CompletableFuture.allOf
            var completableTaskService = ContextHolder.getInstanceOrDefault(ICompletableTaskService.class, new DefaultCompletableTaskService());
            requestTaskMap.getOrCreate(group)
                    .forEach(task -> {
                        task.getFuture().join();
                        completableTaskService.release(task);
                    });
        }
        requestTaskMap.remove(group);
    }

    @Override
    public void shutdown(boolean await) {
        super.shutdown(await);
        SafeRunner.run(requestTaskMap::close);
    }
}
