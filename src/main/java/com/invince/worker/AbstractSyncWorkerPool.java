package com.invince.worker;

import com.invince.worker.collections.ITaskGroups;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
class AbstractSyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult>
        extends StandardWorkerPool<T> {

    protected ITaskGroups<GroupByType, T> requestTaskMap;

    public AbstractSyncWorkerPool(WorkerPoolSetup config) {
        super(config);
    }

    @Override
    void init() {
        // init before newWorker
        requestTaskMap =  config.getHelper().newTaskGroups(config.getName());
        super.init();
    }

    public void enqueueAll(GroupByType group, Collection<T> tasks){
        if(tasks != null) {
            tasks.stream().filter(Objects::nonNull).forEach(one -> {
                enqueue(one);// this put the task in the blocking queue
                addIntoGroup(group, one); // this put task in the group, so we can wait result for a group
            });
        }
    }

    final void addIntoGroup(GroupByType group, T task) {
        if(task == null) {
            return;
        }
        requestTaskMap.getOrCreate(group).add(task);
    }

    public final void waitUntilFinish(GroupByType group) {
        if(requestTaskMap.existNotEmptyGroup(group)) {
            // the custom RedissonCompletableFuture is not working well with CompletableFuture.allOf
            requestTaskMap.getOrCreate(group).stream()
                    .map(BaseTask::getFuture).forEach(CompletableFuture::join);
        }
        requestTaskMap.remove(group);
    }
}
