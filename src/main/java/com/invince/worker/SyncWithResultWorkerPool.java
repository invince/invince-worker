package com.invince.worker;

import java.util.List;
import java.util.function.Function;

public class SyncWithResultWorkerPool
        <T extends AbstractStandardTaskWithResult<SingleResult>, GroupByType, SingleResult, GatheredResult>
        extends AbstractSyncWithResultWorkerPool<T, GroupByType, SingleResult, GatheredResult> {

    public SyncWithResultWorkerPool(WorkerPoolSetup config, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(config, gatherFn);
    }

    public GatheredResult waitResultUntilFinish(GroupByType group) {
        return super.waitResultUntilFinishInternal(group);
    }
}
