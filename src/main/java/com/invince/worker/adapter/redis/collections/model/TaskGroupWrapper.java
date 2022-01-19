package com.invince.worker.adapter.redis.collections.model;

import com.invince.exception.WorkerError;
import com.invince.worker.core.ITaskContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskGroupWrapper implements ITaskContext {

    private String prefix;
    private String key;

    public TaskGroupWrapper(ITaskContext context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        this.prefix = context.getPrefix();
        this.key = context.getKey();
    }

}
