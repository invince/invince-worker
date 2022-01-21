package io.github.invince.worker.adapter.redis.collections.model;

import io.github.invince.worker.core.BaseTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ProcessingTaskWrapper is what we put inside redis RedisProcessingTasks
 * we store the task + the poolUid of which the task is being processed (in distributed mode, to identify which working node)
 * @param <V> the task type
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessingTaskWrapper<V extends BaseTask> implements Serializable {

    private V task;
    private String poolProcessIt;
}
