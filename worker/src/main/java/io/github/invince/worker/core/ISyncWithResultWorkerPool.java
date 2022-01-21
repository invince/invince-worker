package io.github.invince.worker.core;

/**
 * Wait all task finishes in that group and gather all SingleResult to a GatheredResult
 * @param <T> task type
 * @param <GroupByType> group key type
 * @param <SingleResult> SingleResult of a single task
 * @param <GatheredResult> GatheredResult is the final result when we merge all task SingleResult of a group together
 */
public interface ISyncWithResultWorkerPool<T extends AbstractStandardTaskWithResult<SingleResult>, GroupByType, SingleResult, GatheredResult>
        extends ISyncWorkerPool<T, GroupByType, SingleResult> {

    /**
     * Wait all task finishes in that group and gather all SingleResult to a GatheredResult
     * @param group the group of your tasks, so you can wait all task of that group finishes
     * @return GatheredResult calculated from SingleResult of all tasks in the same group
     */
    GatheredResult waitResultUntilFinish(GroupByType group);
}
