package com.invince.worker.adapter.redis.collections;

import com.invince.worker.adapter.redis.collections.model.TaskGroupWrapper;
import com.invince.worker.core.collections.ITaskGroups;
import com.invince.worker.core.future.CompletableTaskFuture;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.util.LinkedList;
import java.util.Queue;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * RedisTaskGroups:
 *     each group is a RQueue of taskFuture (on CompletableTaskFuture you can find the task key, task prefix)
 *
 * @param <GroupByType> groupKey type
 * @param <SingleResult> result type for a single task
 */
public class RedisTaskGroups<GroupByType, SingleResult> implements ITaskGroups<GroupByType, SingleResult> {

    private static final String TASK_GROUPS = "$TASK_GROUPS$:::";

    private final RedissonClient redisson;
    private final String workerPoolPrefix;
    private final ICompletableTaskFutureService completableTaskFutureService;

    public RedisTaskGroups(RedissonClient redisson, ICompletableTaskFutureService completableTaskFutureService, String workerPoolPrefix) {
        this.redisson = redisson;
        this.completableTaskFutureService = completableTaskFutureService;
        this.workerPoolPrefix = workerPoolPrefix;
    }


    /**
     * Get or Create the RQueue for that group.
     *  - we create and return a local copy (because we cannot save CompletableTaskFuture on redis, it's not serializable)
     *  - if we add something on that copy, it will trigger the addition to redis RQueue
     *
     * @param group group key
     * @return the queue of taskFuture of that group
     */
    @Override
    public Queue<CompletableTaskFuture<SingleResult>> getOrCreate(GroupByType group) {
        var taskKeysForThisGroup = getRedisQueue(group);
        QueueWithTrigger<CompletableTaskFuture<SingleResult>> rt = new QueueWithTrigger<>() {

            // When other process getOrCreate the queue and add element in it, we trigger add into redis queue
            @Override
            protected void onAdd(CompletableTaskFuture<SingleResult> element) {
                taskKeysForThisGroup.add(new TaskGroupWrapper(element));
            }
        };

        if(taskKeysForThisGroup != null) {
            taskKeysForThisGroup.forEach(
                    oneWrapper -> {
                        CompletableTaskFuture<SingleResult> taskFuture = completableTaskFutureService.getOrWrap(oneWrapper);
                        if(taskFuture != null) {
                            rt.addNoTrigger(taskFuture);
                        }
                    }
            );
        }
        return rt;
    }

    /**
     * Check if group exist and if we have already taskFuture in it
     * @param group group key
     * @return if group exist and if we have already taskFuture in it
     */
    @Override
    public boolean existNotEmptyGroup(GroupByType group) {
        return isEmpty(getRedisQueue(group));
    }

    /**
     * remove the group
     * @param group group key
     */
    @Override
    public void remove(GroupByType group) {
        var queue = getRedisQueue(group);
        queue.clear();
    }


    private RQueue<TaskGroupWrapper> getRedisQueue(GroupByType group) {
        return redisson.getQueue(workerPoolPrefix + TASK_GROUPS + group);
    }

    private abstract static class QueueWithTrigger<E> extends LinkedList<E> {

        abstract protected void onAdd(E element);

        @Override
        public boolean add(E e) {
            onAdd(e);
            return super.add(e);
        }

        boolean addNoTrigger(E e) {
            return super.add(e);
        }
    }
}
