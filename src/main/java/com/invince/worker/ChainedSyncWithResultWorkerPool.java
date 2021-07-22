package com.invince.worker;

import com.invince.exception.WorkerError;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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

    public void enqueue(GroupByType group, SingleResultWithParam param) {
        this.enqueueStarted = true;
        T task = newTask(group, param);
        task.chain(this.poolChain);
        enqueue(task); // this add into blocking queu
        addIntoGroup(group, task); // this put task in the group, so we can wait result for a group
    }

    public void enqueueAll2(GroupByType group, Collection<SingleResultWithParam> paramList) {
        if(paramList != null) {
            paramList.stream().filter(Objects::nonNull).forEach(one -> enqueue(group, one));
        }
    }

    protected abstract T newTask(GroupByType group, SingleResultWithParam param);

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
}
