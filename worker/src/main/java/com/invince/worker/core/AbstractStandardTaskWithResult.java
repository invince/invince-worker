package com.invince.worker.core;

import com.invince.worker.core.future.CompletableTaskFuture;

public abstract class AbstractStandardTaskWithResult<R> extends AbstractTaskWithResult<R>{


    @Override
    final void processInternal(CompletableTaskFuture<R> taskFuture) {
        taskFuture.complete(doProcess(taskFuture));
    }
}
