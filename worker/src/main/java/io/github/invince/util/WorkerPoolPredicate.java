package io.github.invince.util;

import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.IWorkerPool;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;

/**
 * in CompositeWorkerPool, you can put a list of WorkerPoolPredicate,
 * if task match one predicate, it will be enqueue to that pool
 *
 * @param <W> workerPool type
 * @param <T> task type
 */
@AllArgsConstructor
public class WorkerPoolPredicate<W extends IWorkerPool<T>, T extends BaseTask> {

    /**
     * If task matches the predicate, which workerPool will process the task
     */
    @Getter private final W workerPool;

    /**
     * the predicate to check
     */
    @Getter private final Predicate<T> predicate;

}
