package com.invince.worker.core;

import com.invince.exception.WorkerError;
import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * if you want use ChainedSyncWithResultWorkerPool, you need extends your task on this class
 *
 * @param <GroupByType> group key type
 * @param <SingleResultWithParam> hold param + result through all chained pool. each pool will generate a task from this SingleResultWithParam and process it
 */
@Slf4j
public abstract class AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam>
        extends AbstractTaskWithResult<SingleResultWithParam>{

    protected GroupByType groupBy;
    protected SingleResultWithParam param;

    private ChainedSyncWithResultWorkerPool<?,GroupByType,SingleResultWithParam,?> nextPool;
    private boolean initialized = false;

    /**
     * chain the task to next pool
     * @param nextPool next pool
     * @return self
     */
    public AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam> chain(
            ChainedSyncWithResultWorkerPool<?,GroupByType,SingleResultWithParam,?> nextPool) {
        this.nextPool = nextPool;
        return this;
    }


    /**
     * init the task
     * @param group group type
     * @param param param + result
     * @return self
     */
    public AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam> init(GroupByType group, SingleResultWithParam param) {
        this.param = param;
        this.groupBy = group;
        this.initialized = true;
        return this;
    }

    /**
     * process on this pool, then enqueue to next pool
     * @param taskFuture taskFuture to control task finish or fail
     */
    @Override
    final void processInternal(CompletableTaskFuture<SingleResultWithParam> taskFuture){
        WorkerError.verify("Task not initialized").isTrue(initialized);
        SingleResultWithParam result = doProcess(taskFuture);
        if(nextPool != null){
            log.debug("Enqueue into the chained worker pool {}", nextPool.getClass().getSimpleName());
            nextPool.enqueue(groupBy, result);
        }
        taskFuture.complete(result);
    }

}
