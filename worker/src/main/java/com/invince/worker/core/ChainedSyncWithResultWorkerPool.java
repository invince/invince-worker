package com.invince.worker.core;

import com.invince.exception.WorkerError;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * You can chain workerPool together, then enqueue a SingleResultWithParam to first, once it's finished (result will be also set to that SingleResultWithParam),
 * We pass the SingleResultWithParam (this time can with some additional result) to the next chained pool
 * @param <T> task type
 * @param <GroupByType> group key type can be string long for example
 * @param <SingleResultWithParam> hold param + result through all chained pool. each pool will generate a task from this SingleResultWithParam and process it
 * @param <GatheredResult> the final gatheredResult calculated from all SingleResultWithParam
 */
public abstract class ChainedSyncWithResultWorkerPool
        <T extends AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam>, GroupByType, SingleResultWithParam, GatheredResult>
        extends AbstractSyncWithResultWorkerPool<T, GroupByType, SingleResultWithParam, GatheredResult> {

    private boolean enqueueStarted = false;

    private ChainedSyncWithResultWorkerPool<?, GroupByType, SingleResultWithParam, GatheredResult> poolChain;

    public ChainedSyncWithResultWorkerPool(WorkerPoolSetup config) {
        super(config, null);
    }

    public ChainedSyncWithResultWorkerPool(WorkerPoolSetup config, Function<List<SingleResultWithParam>, GatheredResult> gatherFn) {
        super(config, gatherFn);
    }

    /**
     * Chain another ChainedSyncWithResultWorkerPool.
     * if this pool has already chained pool, then the new pool will be chain to the end
     *
     * @param nextPool the pool to chain
     * @return self
     */
    public ChainedSyncWithResultWorkerPool<T, GroupByType, SingleResultWithParam, GatheredResult> chain(
            ChainedSyncWithResultWorkerPool<?, GroupByType, SingleResultWithParam, GatheredResult> nextPool) {
        if (nextPool != null) {
            WorkerError.verify("Already enqueued some task, you cannot chain workerpool anymore")
                    .isFalse(enqueueStarted);
            if (this.poolChain == null) {
                this.poolChain = nextPool;
            } else {
                this.poolChain.chain(nextPool);
            }
        }
        return this;
    }

    /**
     * Enqueue single task parameter
     * @param group the group of your tasks, so you can wait all task of that group finishes
     * @param param parameter to generate the task
     */
    public void enqueue(GroupByType group, SingleResultWithParam param) {
        this.enqueueStarted = true;
        T task = newTask(group, param);
        task.chain(this.poolChain);
        enqueue(task); // this add into blocking queu
        addIntoGroup(group, task); // this put task in the group, so we can wait result for a group
    }

    /**
     * You can enqueue a list of task parameters
     * @param group the group of your tasks, so you can wait all task of that group finishes
     * @param paramList parameter to generate the tasks
     */
    public void enqueueAll2(GroupByType group, Collection<SingleResultWithParam> paramList) {
        if(paramList != null) {
            paramList.stream().filter(Objects::nonNull).forEach(one -> enqueue(group, one));
        }
    }

    /**
     * Tell pool how to generate a task from the task param
     * @param group the group of your tasks, so you can wait all task of that group finishes
     * @param param parameter to generate the task
     * @return a new task from the task parameter
     */
    protected abstract T newTask(GroupByType group, SingleResultWithParam param);

    /**
     * Wait all task finishes in that group (in this pool and also chained pool) and gather all SingleResult to a GatheredResult
     * @param group the group of your tasks, so you can wait all task of that group finishes
     * @return GatheredResult calculated from SingleResult of all tasks in the same group
     */
    public GatheredResult waitResultUntilFinish(GroupByType group) {
        GatheredResult result;
        if(poolChain == null) {
            result = this.waitResultUntilFinishInternal(group);
        } else {
            this.waitUntilFinish(group);
            result = this.poolChain.waitResultUntilFinish(group); // so we tak result from the last one
        }
        return result;
    }

    /**
     * Wait all task finishes in that group (in this pool and also chained pool)
     * @param group the group of your tasks, so you can wait all task of that group finishes
     */
    public void waitChainUntilFinish(GroupByType group) {
        this.waitUntilFinish(group);
        if(poolChain != null) {
            this.poolChain.waitChainUntilFinish(group);
        }
    }
}
