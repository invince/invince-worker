package io.github.invince.worker.demo.workerPool;

import io.github.invince.worker.core.AbstractStandardTaskWithResult;
import io.github.invince.worker.core.future.CompletableTaskFuture;

public class Plus1Task extends AbstractStandardTaskWithResult<Integer> {
    int value;

    public Plus1Task(int value) {
        this.value = value;
    }

    @Override
    protected Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
        return value + 1;
    }
}
