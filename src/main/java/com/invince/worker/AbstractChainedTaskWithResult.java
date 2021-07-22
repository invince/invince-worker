package com.invince.worker;

import com.invince.exception.WorkerError;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam>
        extends AbstractTaskWithResult<SingleResultWithParam>{

    protected GroupByType groupBy;
    protected SingleResultWithParam param;

    private ChainedSyncWithResultWorkerPool<?,GroupByType,SingleResultWithParam,?> nextPool;
    private boolean initialized = false;

    AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam> chain(
            ChainedSyncWithResultWorkerPool<?,GroupByType,SingleResultWithParam,?> nextPool) {
        this.nextPool = nextPool;
        return this;
    }


    AbstractChainedTaskWithResult<GroupByType, SingleResultWithParam> init(GroupByType group, SingleResultWithParam param) {
        this.param = param;
        this.groupBy = group;
        this.initialized = true;
        return this;
    }

    @Override
    final void processInternal(){
        WorkerError.verify("Task not initialized").isTrue(initialized);
        SingleResultWithParam result = doProcess();
        if(nextPool != null){
            log.debug("Enqueue into the chained worker pool {}", nextPool.getClass().getSimpleName());
            nextPool.enqueue(groupBy, result);
        }
        getFuture().complete(result);
    }

}
