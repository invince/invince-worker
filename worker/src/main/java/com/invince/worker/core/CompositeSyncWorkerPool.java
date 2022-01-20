package com.invince.worker.core;

import com.invince.exception.WorkerError;
import com.invince.util.SafeRunner;
import com.invince.util.WorkerPoolPredicate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * in additional of CompositeWorkerPool, you can waitUntilFinish
 *
 * @param <T>            task type
 * @param <GroupByType>  group key type
 * @param <SingleResult> singleResult of single task
 */
@Slf4j
public class CompositeSyncWorkerPool<T extends BaseTask<SingleResult>, GroupByType, SingleResult>
        implements ISyncWorkerPool<T, GroupByType, SingleResult> {

    private final List<WorkerPoolPredicate<AbstractSyncWorkerPool<T, GroupByType, SingleResult>, T>> pools;
    private final AbstractSyncWorkerPool<T, GroupByType, SingleResult> defaultPool;

    public CompositeSyncWorkerPool(AbstractSyncWorkerPool<T, GroupByType, SingleResult> defaultPool,
                                   List<WorkerPoolPredicate<AbstractSyncWorkerPool<T, GroupByType, SingleResult>, T>> pools) {
        this.pools = pools;
        WorkerError.verify("Null default Pool").nonNull(defaultPool);
        this.defaultPool = defaultPool;
    }

    /**
     * @return todo list size, used in monitor service
     */
    @Override
    public int getToDoListSize() {
        int total = defaultPool.getToDoListSize();
        if (!isEmpty(pools)) {
            total += pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .mapToDouble(IWorkerPool::getToDoListSize).sum();
        }
        return total;
    }

    /**
     * @return processing list size, used in monitor service
     */
    @Override
    public int getProcessingListSize() {
        int total = defaultPool.getProcessingListSize();
        if (!isEmpty(pools)) {
            total += pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .mapToDouble(IWorkerPool::getProcessingListSize).sum();
        }
        return total;
    }

    /**
     * @return nb of permanent worker started, used in monitor service
     */
    @Override
    public int getPermanentWorkerSize() {
        int total = defaultPool.getPermanentWorkerSize();
        if (!isEmpty(pools)) {
            total += pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .mapToDouble(IWorkerPool::getPermanentWorkerSize).sum();
        }
        return total;
    }

    /**
     * Enqueue a task in the workpool
     * - when you enqueue a task, we'll check workerPoolPredicate one by one, if predicate matches, the task will be redirected to that pool
     *
     * @param task task
     * @return the task
     */
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

    /**
     * @param key task key
     * @return if task exists in todo list
     */
    @Override
    public boolean existToDoTask(String key) {
        if (!isEmpty(pools)) {
            return pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .anyMatch(one -> one.existToDoTask(key))
                    || defaultPool.existToDoTask(key);
        }
        return defaultPool.existToDoTask(key);
    }

    /**
     * @param key task key
     * @return if task exists in processing list
     */
    @Override
    public boolean existProcessingTask(String key) {
        if (!isEmpty(pools)) {
            return pools.stream().map(WorkerPoolPredicate::getWorkerPool)
                    .anyMatch(one -> one.existProcessingTask(key))
                    || defaultPool.existProcessingTask(key);
        }
        return defaultPool.existProcessingTask(key);
    }

    /**
     * remove the task via
     *
     * @param key task key
     * @return the removed task
     */
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

    /**
     * shutdown the all the workpool
     *
     * @param await if we wait current process finish
     */
    @Override
    public void shutdown(boolean await) {
        if (!isEmpty(pools)) {
            pools.stream().map(WorkerPoolPredicate::getWorkerPool).forEach(one -> SafeRunner.run(() -> one.shutdown(await)));
        }
        defaultPool.shutdown(await);
    }

    /**
     * cancel a task (no matter it's in todo list or processing list)
     * - we'll match first which pool process it, then call pool.cancelTask
     *
     * @param key task list
     */
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

    /**
     * Enqueue tasks in a group, you can call this separately, all task in same group counts
     * - for each task we'll match the pool to process it
     * @param groupName groupName
     * @param tasks tasks
     */
    @Override
    public void enqueueAll(GroupByType groupName, Collection<T> tasks) {
        if (isEmpty(tasks)) {
            return;
        }
        if (!isEmpty(pools)) {
            for (T task : tasks) {
                var poolFound = pools.stream()
                        .filter(one -> one.getPredicate() != null && one.getWorkerPool() != null && one.getPredicate().test(task))
                        .map(WorkerPoolPredicate::getWorkerPool)
                        .findFirst().orElse(defaultPool);
                poolFound.addIntoGroup(groupName, task);
            }
        } else {
            defaultPool.enqueueAll(groupName, tasks);
        }
    }

    /**
     * wait all task in same group finishes
     * - for each task we'll match the pool, then waitUntilFinish
     * @param groupName groupName
     */
    @Override
    public void waitUntilFinish(GroupByType groupName) {
        if (!isEmpty(pools)) {
            pools.stream().map(WorkerPoolPredicate::getWorkerPool).forEach(one -> one.waitUntilFinish(groupName));
        }
        defaultPool.waitUntilFinish(groupName);
    }

    /**
     * cancel all tasks in same group
     * @param groupName groupName
     */
    @Override
    public void cancelGroup(GroupByType groupName) {
        if (!isEmpty(pools)) {
            pools.stream().map(WorkerPoolPredicate::getWorkerPool).forEach(one -> one.cancelGroup(groupName));
        }
        defaultPool.cancelGroup(groupName);
    }
}
