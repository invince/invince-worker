package com.invince.worker.core;

import java.util.List;
import java.util.function.Function;


/**
 * in additional of above, you can get the GatheredResult of all the task (each has SingleResult) in the group waitResultUntilFinish
 * cf example: SyncWithResultWorkerPoolExample
 *
 * @param <T> task type
 * @param <GroupByType> group key type
 * @param <SingleResult> SingleResult of a single task
 * @param <GatheredResult> GatheredResult is the final result when we merge all task SingleResult of a group together
 */
public class SyncWithResultWorkerPool
        <T extends AbstractStandardTaskWithResult<SingleResult>, GroupByType, SingleResult, GatheredResult>
        extends AbstractSyncWithResultWorkerPool<T, GroupByType, SingleResult, GatheredResult>
        implements ISyncWithResultWorkerPool<T, GroupByType, SingleResult, GatheredResult> {

    /**
     * @param config   workerPoolSetup
     * @param gatherFn how to merge all SingleResult into GatheredResult
     */
    public SyncWithResultWorkerPool(WorkerPoolSetup config, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(config, gatherFn);
    }


    /**
     * Wait all task finishes in that group and gather all SingleResult to a GatheredResult
     *
     * @param group the group of your tasks, so you can wait all task of that group finishes
     * @return GatheredResult calculated from SingleResult of all tasks in the same group
     */
    public GatheredResult waitResultUntilFinish(GroupByType group) {
        return super.waitResultUntilFinishInternal(group);
    }
}
