package com.invince.worker.core;

import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask extends BaseTask<Void> {
    @Override
    final void processInternal(CompletableTaskFuture<Void> taskFuture) {
        doProcess(taskFuture);
        taskFuture.complete(null);
    }

    protected abstract void doProcess(CompletableTaskFuture<Void> taskFuture);
}
