package com.invince.worker.core;

import com.invince.exception.TaskCancelled;
import com.invince.exception.WorkerWarning;
import com.invince.util.SafeRunner;
import com.invince.worker.core.collections.ITaskGroups;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;

/**
 * in additional of IWorkerPool, you can group your task when you enqueue them, and **waitUntilFinish** for all tasks in that group
 * @param <T> task type
 * @param <GroupByType> group key type
 * @param <SingleResult> SingleResult of a single task
 */
@Slf4j
class AbstractSyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult>
        extends StandardWorkerPool<T> implements ISyncWorkerPool<T, GroupByType, SingleResult> {

    protected ITaskGroups<GroupByType, SingleResult> requestTaskMap;

    public AbstractSyncWorkerPool(WorkerPoolSetup config) {
        super(config);
    }

    /**
     * we need create the requestTaskMap to save the map between group -> tasks in this group
     */
    @Override
    protected void beforeInit() {
        // init before newWorker
        requestTaskMap = config.getHelper().newTaskGroups(config.getName());
    }

    /**
     * Enqueue tasks in a group, you can call this separately, all task in same group counts
     * @param groupName groupName
     * @param tasks tasks
     */
    @Override
    public void enqueueAll(GroupByType groupName, Collection<T> tasks) {
        if (tasks != null) {
            tasks.stream().filter(Objects::nonNull).forEach(one -> {
                enqueue(one);// this put the task in the blocking queue
                addIntoGroup(groupName, one); // this put task in the group, so we can wait result for a group
            });
        }
    }

    final void addIntoGroup(GroupByType group, T task) {
        if (task == null) {
            return;
        }
        requestTaskMap.getOrCreate(group).add(completableTaskFutureService.getOrWrap(task));
    }

    /**
     * cancel all tasks in same group
     * @param groupName groupName
     */
    @Override
    public void cancelGroup(GroupByType groupName) {
        if (requestTaskMap.existNotEmptyGroup(groupName)) {
            requestTaskMap.getOrCreate(groupName).forEach(taskFuture -> {
                var key = taskFuture.getKey();
                cancelTask(key);
            });
        }
    }

    /**
     * wait all task in same groupName finishes
     * @param groupName groupName
     */
    @Override
    public final void waitUntilFinish(GroupByType groupName) {
        if (requestTaskMap.existNotEmptyGroup(groupName)) {
            // the custom RedissonCompletableFuture is not working well with CompletableFuture.allOf
            requestTaskMap.getOrCreate(groupName)
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
        requestTaskMap.remove(groupName);
    }

    /**
     * in addition, close the requestTaskMap
     * @param await if we wait current process finish
     */
    @Override
    public void shutdown(boolean await) {
        super.shutdown(await);
        SafeRunner.run(requestTaskMap::close);
    }
}
