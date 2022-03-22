package io.github.invince.worker.adapter.redis.helper;

import io.github.invince.worker.adapter.redis.collections.RedisProcessingTasks;
import io.github.invince.worker.adapter.redis.collections.RedisTaskGroups;
import io.github.invince.worker.adapter.redis.collections.RedisTodoTasks;
import io.github.invince.worker.adapter.redis.future.RedisCompletableTaskFutureService;
import io.github.invince.worker.adapter.redis.future.RedissonCompletableTaskFutureHelper;
import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.WorkerPoolSetup;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.ITaskGroups;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.core.helper.IWorkerPoolHelper;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;
import io.github.invince.spring.WorkerPoolConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Redis version of IWorkerPoolHelper
 */
@Primary
@Profile(WorkerPoolConfiguration.PROFILE_REDIS)
@Service
public class RedisWorkerPoolHelper implements IWorkerPoolHelper {

    private final RedissonClient client;
    private final ICompletableTaskFutureService completableTaskFutureService;
    private final String defaultName = UUID.randomUUID().toString();

    @Autowired
    public RedisWorkerPoolHelper(RedissonClient client, RedissonCompletableTaskFutureHelper helper) {
        this.client = client;
        this.completableTaskFutureService = new RedisCompletableTaskFutureService(client, helper);
    }

    /**
     * Create a new RedisTodoTasks
     *
     * @param setup WorkerPoolSetup
     * @param poolUid the poolUid of the workerPool, this will be used to identify the task is processing on which pool/node (NOTE: every node shall have its own pool, and every pool has different poolUid)
     * @return RedisTodoTasks
     */
    @Override
    public IToDoTasks newToDoTasks(WorkerPoolSetup setup, String poolUid) {
        return new RedisTodoTasks(client, poolUid);
    }

    /**
     * Create a new RedisProcessingTasks
     *
     * @param setup WorkerPoolSetup
     * @param poolUid the poolUid of the workerPool, this will be used to identify the task is processing on which pool/node (NOTE: every node shall have its own pool, and every pool has different poolUid)
     * @param <T> the Task Type
     * @return RedisProcessingTasks
     */
    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(WorkerPoolSetup setup, String poolUid) {
        return new RedisProcessingTasks<>(client, setup, poolUid);
    }

    /**
     * Create a new RedisTaskGroups, to map which task is in which group
     *
     * @param setup WorkerPoolSetup
     * @param <GroupBy> group key type
     * @param <SingleResult> singleResult of a single task
     * @return RedisTaskGroups
     */
    @Override
    public <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(WorkerPoolSetup setup) {
        return new RedisTaskGroups<>(client, completableTaskFutureService, getName(setup));
    }


    /**
     * RedisCompletableTaskFutureService to help generate/simulate distribute CompletableTaskFuture from a baseTask
     * @return RedisCompletableTaskFutureService
     */
    @Override
    public ICompletableTaskFutureService getCompletableTaskFutureService() {
        return completableTaskFutureService;
    }

    private String getName(WorkerPoolSetup setup) {
        return Optional.ofNullable(setup).map(WorkerPoolSetup::getQueueName).orElse(defaultName);
    }
}
