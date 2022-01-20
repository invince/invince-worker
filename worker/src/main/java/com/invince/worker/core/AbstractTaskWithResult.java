package com.invince.worker.core;

import com.invince.worker.core.future.CompletableTaskFuture;

/**
 * internal class for AbstractStandardTaskWithResult and AbstractChainedTaskWithResult
 * @param <R> result type
 */
abstract class AbstractTaskWithResult<R> extends BaseTask<R> {

    protected abstract R doProcess(CompletableTaskFuture<R> taskFuture);
}
