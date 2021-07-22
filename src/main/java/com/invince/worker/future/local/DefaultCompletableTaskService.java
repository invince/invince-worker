package com.invince.worker.future.local;

import com.invince.exception.WorkerError;
import com.invince.worker.BaseTask;
import com.invince.worker.future.ICompletableTaskService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCompletableTaskService implements ICompletableTaskService {

    private static final ConcurrentHashMap<String, CompletableFuture<?>> futureMap = new ConcurrentHashMap<>();

    @Override
    public <T> CompletableFuture<T> getOrWrap(BaseTask tBaseTask) {
        WorkerError.verify("Not able to wrap task").nonNull(tBaseTask);
        futureMap.putIfAbsent(tBaseTask.getKey(), new CompletableFuture<>());
        return (CompletableFuture<T>) futureMap.get(tBaseTask.getKey());
    }
}
