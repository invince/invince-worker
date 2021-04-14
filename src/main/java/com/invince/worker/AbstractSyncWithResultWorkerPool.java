package com.invince.worker;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractSyncWithResultWorkerPool<T extends AbstractTaskWithResult<SingleResult>, GroupByType, SingleResult, GatheredResult>
        extends AbstractSyncWorkerPool<T, GroupByType, SingleResult> {

    private final Function<List<SingleResult>, GatheredResult> gatherFn;

    public AbstractSyncWithResultWorkerPool(int maxWorker, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(maxWorker);
        this.gatherFn = gatherFn;
    }

    // NOTE: you can only wait one time, since we remove the group once you have the result
    GatheredResult waitResultUntilFinishInternal(GroupByType group) {
        GatheredResult rt = null;
        if(requestTaskMap.containsKey(group) && !requestTaskMap.get(group).isEmpty()) {
            rt = gatherFn.apply(
                    requestTaskMap.get(group).stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );
            requestTaskMap.remove(group);
        }
        return rt;
    }
}
