package com.invince.worker.adapter.redis.future;

import com.invince.exception.WorkerError;
import com.invince.exception.WorkerRemoteError;
import com.invince.worker.core.ITaskIdentify;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * To simplify the distribute join, the holder contains either result or exception
 * @param <T> result type
 */
@Accessors(chain = true)
@Setter
@NoArgsConstructor
public class RedissonCompletableFutureResultHolder<T> implements ITaskIdentify, Serializable {

    public RedissonCompletableFutureResultHolder(ITaskIdentify context) {
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
