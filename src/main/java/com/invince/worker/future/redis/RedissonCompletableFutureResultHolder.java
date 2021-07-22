package com.invince.worker.future.redis;

import com.invince.exception.WorkerRemoteError;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
@Setter
// to simplify the join, the holder contains either result or exception
public class RedissonCompletableFutureResultHolder <T> implements Serializable {

    T result;

    WorkerRemoteError exception;
}
