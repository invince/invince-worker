package com.invince.worker.core;

import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Since we use blocking Queue, we need a sign to stop the worker/whole workerPool
 */
@Slf4j
public class FinishTask extends AbstractTask {

    @Override
    protected void doProcess(CompletableTaskFuture<Void> taskFuture) {
        log.debug("Finish task process");
    }
}
