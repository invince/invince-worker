package com.invince.worker.core.future;

import com.invince.worker.core.ITaskContext;

public interface ICompletableTaskFutureService {
    <SingleResult> CompletableTaskFuture<SingleResult> getOrWrap(ITaskContext taskContext);

    void release(ITaskContext task);
}
