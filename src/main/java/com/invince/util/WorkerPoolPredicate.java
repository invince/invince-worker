package com.invince.util;

import com.invince.worker.BaseTask;
import com.invince.worker.IWorkerPool;
import lombok.Getter;

import java.util.function.Predicate;

public class WorkerPoolPredicate<W extends IWorkerPool<T>, T extends BaseTask> {

    @Getter private final W workerPool;
    @Getter private final Predicate<T> predicate;

    public WorkerPoolPredicate(W workerPool, Predicate<T> predicate) {
        this.workerPool = workerPool;
        this.predicate = predicate;
    }
}
