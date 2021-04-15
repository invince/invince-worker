package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
class AbstractSyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult>
        extends StandardWorkerPool<T> {

    protected final Map<GroupByType, ConcurrentLinkedQueue<T>> requestTaskMap = new ConcurrentHashMap<>();

    public AbstractSyncWorkerPool(int maxWorker) {
        super(maxWorker);
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
        requestTaskMap.putIfAbsent(group, new ConcurrentLinkedQueue<>());
        requestTaskMap.get(group).add(task);
    }

    public final void waitUntilFinish(GroupByType group) {
        if(requestTaskMap.containsKey(group) && !requestTaskMap.get(group).isEmpty()) {
            CompletableFuture.allOf(requestTaskMap.get(group).toArray(new BaseTask<?>[0])).join();
        }
        requestTaskMap.remove(group);
    }
}
