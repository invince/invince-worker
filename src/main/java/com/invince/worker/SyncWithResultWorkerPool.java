package com.invince.worker;

import java.util.List;
import java.util.function.Function;

public class SyncWithResultWorkerPool
        <T extends AbstractStandardTaskWithResult<SingleResult>, GroupByType, SingleResult, GatheredResult>
        extends AbstractSyncWithResultWorkerPool<T, GroupByType, SingleResult, GatheredResult> {

    public SyncWithResultWorkerPool(int maxWorker, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(maxWorker, gatherFn);
    }

    public GatheredResult waitResultUntilFinish(GroupByType group) {
        return super.waitResultUntilFinishInternal(group);
    }
}
