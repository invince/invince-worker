package com.invince.worker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StandardSyncWorkerWithResultPool <T extends AbstractSyncTaskWithResult<SingleResult>,
        GroupByType, SingleResult, GatheredResult>
        extends StandardWorkerPool<T> {

    private Function<List<SingleResult>, GatheredResult> gatherFn;
    private final Map<GroupByType, ConcurrentLinkedQueue<T>> requestTaskMap = new ConcurrentHashMap<>();

    public StandardSyncWorkerWithResultPool(int maxWorker, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(maxWorker);
        this.gatherFn = gatherFn;
    }

    public void enqueueAll(GroupByType groupBy, Collection<T> tasks) {
        if(!requestTaskMap.containsKey(groupBy)) {
            requestTaskMap.put(groupBy, new ConcurrentLinkedQueue<>());
        }
        if(tasks != null) {
            for (T t : tasks) {
                enqueue(t);
                requestTaskMap.get(groupBy).add(t);
            }
        }
    }

    // NOTE: since this method clear task from requestTaskMap, it works only once, the second time you will get an empty list
    public GatheredResult waitResultUntilFinish(GroupByType groupBy) {
        GatheredResult rt = null;
        if(requestTaskMap.get(groupBy) != null && gatherFn != null) {
            rt = gatherFn.apply(requestTaskMap.get(groupBy).stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );
        }
        requestTaskMap.remove(groupBy);
        return rt;
    }
}
