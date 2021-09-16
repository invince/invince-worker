package com.invince.worker.collections.redis;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IProcessingTasks;
import com.invince.worker.collections.ITaskGroups;
import com.invince.worker.collections.IToDoTasks;
import com.invince.worker.collections.IWorkerPoolHelper;
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

    @Autowired
    public RedisWorkerPoolHelper(RedissonClient client) {
        this.client = client;
    }

    @Override
    public IToDoTasks newToDoTasks(String name) {
        return new RedisTodoTasks(client, name);
    }

    @Override
    public <T extends BaseTask> IProcessingTasks<String, T> newProcessingTasks(String name) {
        return new RedisProcessingTasks<>(client, name);
    }

    @Override
    public <GroupBy, T extends BaseTask> ITaskGroups<GroupBy, T> newTaskGroups(String name) {
        return new RedisTaskGroups<>(client, name);
    }
}
