package com.invince.worker.adapter.redis.future;

import com.invince.exception.WorkerError;
import com.invince.exception.WorkerRemoteError;
import com.invince.worker.core.ITaskContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
@Setter
@NoArgsConstructor
// to simplify the join, the holder contains either result or exception
public class RedissonCompletableFutureResultHolder<T> implements ITaskContext, Serializable {

    public RedissonCompletableFutureResultHolder(ITaskContext context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        this.prefix = context.getPrefix();
        this.key = context.getKey();
    }

    @Getter
    String prefix;
    @Getter
    String key;
    T result;
    WorkerRemoteError exception;
}
