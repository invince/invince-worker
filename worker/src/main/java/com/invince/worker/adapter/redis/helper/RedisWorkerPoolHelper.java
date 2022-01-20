package com.invince.worker.adapter.redis.helper;

import com.invince.worker.adapter.redis.collections.RedisProcessingTasks;
import com.invince.worker.adapter.redis.collections.RedisTaskGroups;
import com.invince.worker.adapter.redis.collections.RedisTodoTasks;
import com.invince.worker.adapter.redis.future.RedisCompletableTaskFutureService;
import com.invince.worker.adapter.redis.future.RedissonCompletableTaskFutureHelper;
import com.invince.worker.core.BaseTask;
import com.invince.worker.core.collections.IProcessingTasks;
import com.invince.worker.core.collections.ITaskGroups;
import com.invince.worker.core.collections.IToDoTasks;
import com.invince.worker.core.helper.IWorkerPoolHelper;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.invince.spring.WorkerConfig.PROFILE_REDIS;

/**
 * Redis version of IWorkerPoolHelper
 */
@Primary
@Profile(PROFILE_REDIS)
@Service
public class RedisWorkerPoolHelper implements IWorkerPoolHelper {

    private final RedissonClient client;
    private final ICompletableTaskFutureService completableTaskFutureService;

    @Autowired
    public RedisWorkerPoolHelper(RedissonClient client, RedissonCompletableTaskFutureHelper helper) {
        this.client = client;
        this.completableTaskFutureService = new RedisCompletableTaskFutureService(client, helper);
    }

    /**
     * Create a new RedisTodoTasks
     * @param workerPoolName name to identify this todo list is for which workerPool
     * @return RedisTodoTasks
     */
    @Override
    public IToDoTasks newToDoTasks(String workerPoolName) {
        return new RedisTodoTasks(client, workerPoolName);
    }

    /**
     * Create a new RedisProcessingTasks
     * @param workerPoolName name to identify this todo list is for which workerPool
     * @param poolUid the poolUid of the workerPool, this will be used to identify the task is processing on which pool/node (NOTE: every node shall have its own pool, and every pool has different poolUid)
     * @param <T> the Task Type
     * @return RedisProcessingTasks
     */
    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String workerPoolName, String poolUid) {
        return new RedisProcessingTasks<>(client, workerPoolName, poolUid);
    }

    /**
     * Create a new RedisTaskGroups, to map which task is in which group
     * @param workerPoolName not useful in local mode
     * @param <GroupBy> group key type
     * @param <SingleResult> singleResult of a single task
     * @return RedisTaskGroups
     */
    @Override
    public <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String workerPoolName) {
        return new RedisTaskGroups<>(client, completableTaskFutureService, workerPoolName);
    }


    /**
     * RedisCompletableTaskFutureService to help generate/simulate distribute CompletableTaskFuture from a baseTask
     * @return RedisCompletableTaskFutureService
     */
    @Override
    public ICompletableTaskFutureService getCompletableTaskFutureService() {
        return completableTaskFutureService;
    }
}
