package com.invince.worker.future.local;

import com.invince.exception.WorkerError;
import com.invince.worker.BaseTask;
import com.invince.worker.future.ICompletableTaskService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultCompletableTaskService implements ICompletableTaskService {

    private static final ConcurrentHashMap<String, CompletableFuture<?>> futureMap = new ConcurrentHashMap<>();

    @Override
    public <T> CompletableFuture<T> getOrWrap(BaseTask tBaseTask) {
        WorkerError.verify("Not able to wrap task").nonNull(tBaseTask);
        futureMap.putIfAbsent(tBaseTask.getKey(), new CompletableFuture<>());
        return (CompletableFuture<T>) futureMap.get(tBaseTask.getKey());
    }

    @Override
    public void release(BaseTask task) {
        // we cannot do this in completableFuture.whenComplete, because it can complete before we call waitUntilFinish, so we do a manual clean
        WorkerError.verify("Not able to wrap task").nonNull(task);
        futureMap.remove(task.getKey());
    }
}
