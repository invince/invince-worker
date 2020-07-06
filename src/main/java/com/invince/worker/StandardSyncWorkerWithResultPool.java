package com.invince.worker;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StandardSyncWorkerWithResultPool <T extends AbstractSyncTaskWithResult<SingleResult>,
        GroupByType, SingleResult, GatheredResult>
        extends StandardSyncWorkerPool<T, GroupByType> {

    private Function<List<SingleResult>, GatheredResult> gatherFn;

    public StandardSyncWorkerWithResultPool(int maxWorker, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(maxWorker);
        this.gatherFn = gatherFn;
    }

    // NOTE: since this method clear task from requestTaskMap, it works only once, the second time you will get an empty list
    public GatheredResult waitResultUntilFinish(GroupByType groupBy) {
        GatheredResult rt = null;
        doWait(groupBy);
        if(requestTaskMap.get(groupBy) != null && gatherFn != null) {
            rt = gatherFn.apply(requestTaskMap.get(groupBy).stream()
                    .map(AbstractSyncTaskWithResult::getResult)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );
        }
        requestTaskMap.remove(groupBy);
        return rt;
    }
}
