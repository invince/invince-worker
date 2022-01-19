package com.invince.worker.core;

import com.invince.exception.WorkerError;
import com.invince.worker.core.future.CompletableTaskFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam>
        extends AbstractTaskWithResult<SingleResultWithParam>{

    protected GroupByType groupBy;
    protected SingleResultWithParam param;

    private ChainedSyncWithResultWorkerPool<?,GroupByType,SingleResultWithParam,?> nextPool;
    private boolean initialized = false;

    public AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam> chain(
            ChainedSyncWithResultWorkerPool<?,GroupByType,SingleResultWithParam,?> nextPool) {
        this.nextPool = nextPool;
        return this;
    }


    public AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam> init(GroupByType group, SingleResultWithParam param) {
        this.param = param;
        this.groupBy = group;
        this.initialized = true;
        return this;
    }

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
