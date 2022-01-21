package com.invince.worker.core;

import com.invince.worker.core.future.CompletableTaskFuture;

/**
 * if you want use SyncWithResultWorkerPool, you need extends your task on this class
 * @param <R> result type
 */
public abstract class AbstractStandardTaskWithResult<R> extends AbstractTaskWithResult<R>{

    @Override
    final void processInternal(CompletableTaskFuture<R> taskFuture) {
        taskFuture.complete(doProcess(taskFuture));
    }
}
