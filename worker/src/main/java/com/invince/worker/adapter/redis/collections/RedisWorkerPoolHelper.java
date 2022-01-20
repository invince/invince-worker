package com.invince.worker.adapter.redis.collections;

import com.invince.worker.adapter.redis.future.RedisCompletableTaskFutureService;
import com.invince.worker.adapter.redis.future.RedissonCompletableTaskFutureHelper;
import com.invince.worker.core.BaseTask;
import com.invince.worker.core.collections.IProcessingTasks;
import com.invince.worker.core.collections.ITaskGroups;
import com.invince.worker.core.collections.IToDoTasks;
import com.invince.worker.core.collections.IWorkerPoolHelper;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.invince.spring.WorkerConfig.PROFILE_REDIS;

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

    @Override
    public IToDoTasks newToDoTasks(String name) {
        return new RedisTodoTasks(client, name);
    }

    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name, String poolUid) {
        return new RedisProcessingTasks<>(client, name, poolUid);
    }

    @Override
    public <GroupBy, SingleResult> ITaskGroups<GroupBy, SingleResult> newTaskGroups(String name) {
        return new RedisTaskGroups<>(client, completableTaskFutureService, name);
    }

    @Override
    public ICompletableTaskFutureService getCompletableTaskFutureService() {
        return completableTaskFutureService;
    }
}
