package com.invince.worker.core;

import com.invince.worker.core.future.CompletableTaskFuture;

abstract class AbstractTaskWithResult<R> extends BaseTask<R> {

    protected abstract R doProcess(CompletableTaskFuture<R> taskFuture);
}
