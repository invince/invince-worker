package com.invince.worker.core;

import com.invince.exception.TaskCancelled;
import com.invince.exception.WorkerWarning;
import com.invince.util.SafeRunner;
import com.invince.worker.core.collections.ITaskGroups;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;

@Slf4j
class AbstractSyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult>
        extends StandardWorkerPool<T> implements ISyncWorkerPool<T, GroupByType, SingleResult> {

    protected ITaskGroups<GroupByType, SingleResult> requestTaskMap;

    public AbstractSyncWorkerPool(WorkerPoolSetup config) {
        super(config);
    }

    @Override
    protected void beforeInit() {
        // init before newWorker
        requestTaskMap = config.getHelper().newTaskGroups(config.getName());
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
        requestTaskMap.getOrCreate(group).add(completableTaskFutureService.getOrWrap(task));
    }

    @Override
    public void cancelGroup(GroupByType group) {
        if (requestTaskMap.existNotEmptyGroup(group)) {
            requestTaskMap.getOrCreate(group).forEach(taskFuture -> {
                var key = taskFuture.getKey();
                cancelTask(key);
            });
        }
    }

    @Override
    public final void waitUntilFinish(GroupByType group) {
        if (requestTaskMap.existNotEmptyGroup(group)) {
            // the custom RedissonCompletableFuture is not working well with CompletableFuture.allOf
            requestTaskMap.getOrCreate(group)
                    .forEach(taskFuture -> {
                        try {
                            taskFuture.join();
                        } catch (TaskCancelled e) {
                            log.warn("Task {} cancelled, result will be null", e.getKey());
                        } catch (WorkerWarning e) {
                            log.warn(e.getMessage(), e);
                        }
                        completableTaskFutureService.release(taskFuture);
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
