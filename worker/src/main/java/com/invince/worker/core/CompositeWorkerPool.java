package com.invince.worker.core;

import com.invince.exception.WorkerError;
import com.invince.util.SafeRunner;
import com.invince.util.WorkerPoolPredicate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
public class CompositeWorkerPool<T extends BaseTask> implements IWorkerPool<T> {

    private List<WorkerPoolPredicate<StandardWorkerPool<T>, T>> pools;
    private StandardWorkerPool<T> defaultPool;

    public CompositeWorkerPool(StandardWorkerPool<T> defaultPool,
                               List<WorkerPoolPredicate<StandardWorkerPool<T>, T>> pools) {
        this.pools = pools;
        WorkerError.verify("Null default Pool").nonNull(defaultPool);
        this.defaultPool = defaultPool;
    }

    @Override
    public int getToDoListSize() {
        int total = defaultPool.getToDoListSize();
        if (!isEmpty(pools)) {
            total += pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .mapToDouble(IWorkerPool::getToDoListSize).sum();
        }
        return total;
    }

    @Override
    public int getProcessingListSize() {
        int total = defaultPool.getProcessingListSize();
        if (!isEmpty(pools)) {
            total += pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .mapToDouble(IWorkerPool::getProcessingListSize).sum();
        }
        return total;
    }

    @Override
    public int getPermanentWorkerSize() {
        int total = defaultPool.getPermanentWorkerSize();
        if (!isEmpty(pools)) {
            total += pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .mapToDouble(IWorkerPool::getPermanentWorkerSize).sum();
        }
        return total;
    }

    @Override
    public T enqueue(T task) {
        if (!isEmpty(pools)) {
            for (var entry : pools) {
                Predicate<T> predicate = entry.getPredicate();
                IWorkerPool<T> pool = entry.getWorkerPool();

                if (predicate != null && pool != null && predicate.test(task)) {
                    return pool.enqueue(task);
                }
            }
        }
        return defaultPool.enqueue(task);
    }

    @Override
    public boolean existToDoTask(String key) {
        if (!isEmpty(pools)) {
            return pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .anyMatch(one -> one.existToDoTask(key))
                    || defaultPool.existToDoTask(key);
        }
        return defaultPool.existToDoTask(key);
    }

    @Override
    public boolean existProcessingTask(String key) {
        if (!isEmpty(pools)) {
            return pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .anyMatch(one -> one.existProcessingTask(key))
                    || defaultPool.existProcessingTask(key);
        }
        return defaultPool.existProcessingTask(key);
    }

    @Override
    public T removeTask(String key) {
        AtomicReference<T> result = new AtomicReference<>();
        if (!isEmpty(pools)) {
            var poolList = pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .filter(one -> one.existProcessingTask(key)).collect(Collectors.toList());
            if (poolList.size() > 1) {
                log.warn("More than one pool contains this in processing task {}", key);
            }
            for (IWorkerPool<T> onePool : poolList) {
                SafeRunner.run(() -> result.set(onePool.removeTask(key)));
            }
        }
        if (defaultPool.existProcessingTask(key)) {
            return defaultPool.removeTask(key);
        }
        return result.get();
    }

    @Override
    public void shutdown(boolean await) {
        if (!isEmpty(pools)) {
            pools.stream().map(WorkerPoolPredicate::getWorkerPool).forEach(one -> SafeRunner.run(() -> one.shutdown(await)));
        }
        defaultPool.shutdown(await);
    }

    @Override
    public void cancelTask(String key) {
        if (!isEmpty(pools)) {
            var poolList = pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .filter(one -> one.existProcessingTask(key) || one.existToDoTask(key)).collect(Collectors.toList());
            if (poolList.size() > 1) {
                log.warn("More than one pool contains this in processing task {}", key);
            }
            for (IWorkerPool<T> onePool : poolList) {
                SafeRunner.run(() -> onePool.cancelTask(key));
            }
        }
        if (defaultPool.existProcessingTask(key) || defaultPool.existToDoTask(key)) {
            defaultPool.cancelTask(key);
        }
    }
}
