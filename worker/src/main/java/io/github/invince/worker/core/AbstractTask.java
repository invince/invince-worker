package io.github.invince.worker.core;

import io.github.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * if you want use SyncWorkerPool, you need extends your task on this class
 */
@Slf4j
public abstract class AbstractTask extends BaseTask<Void> {
    @Override
    final void processInternal(CompletableTaskFuture<Void> taskFuture) {
        doProcess(taskFuture);
        taskFuture.complete(null);
    }

    protected abstract void doProcess(CompletableTaskFuture<Void> taskFuture);
}
