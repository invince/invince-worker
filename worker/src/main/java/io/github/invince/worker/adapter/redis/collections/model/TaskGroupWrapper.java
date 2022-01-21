package io.github.invince.worker.adapter.redis.collections.model;

import io.github.invince.exception.WorkerError;
import io.github.invince.worker.core.ITaskIdentify;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TaskGroupWrapper is what we put in RedisTaskGroups
 * we store the prefix (to reduce the uuid collision) and key (default is a uuid) of the task
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskGroupWrapper implements ITaskIdentify {

    private String prefix;
    private String key;

    public TaskGroupWrapper(ITaskIdentify context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        this.prefix = context.getPrefix();
        this.key = context.getKey();
    }

}
