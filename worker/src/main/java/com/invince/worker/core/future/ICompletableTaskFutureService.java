package com.invince.worker.core.future;

import com.invince.worker.core.ITaskIdentify;

/**
 * ICompletableTaskFutureService to help generate/simulate CompletableTaskFuture from a baseTask
 */
public interface ICompletableTaskFutureService {

    /**
     * Either create or get (if already created) CompletableTaskFuture from task key + task prefix.
     *
     * @param context task key + task prefix
     * @param <SingleResult> resultType
     * @return CompletableTaskFuture
     */
    <SingleResult> CompletableTaskFuture<SingleResult> getOrWrap(ITaskIdentify context);

    /**
     * At the end of a task, we can release it to free memory usage
     *
     * @param task task key + task prefix
     */
    void release(ITaskIdentify task);
}
