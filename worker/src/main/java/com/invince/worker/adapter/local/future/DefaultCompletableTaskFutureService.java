package com.invince.worker.adapter.local.future;

import com.invince.exception.WorkerError;
import com.invince.worker.core.ITaskContext;
import com.invince.worker.core.future.CompletableTaskFuture;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultCompletableTaskFutureService implements ICompletableTaskFutureService {

    private static final ConcurrentHashMap<String, CompletableTaskFuture<?>> futureMap = new ConcurrentHashMap<>();

    @Override
    public <T> CompletableTaskFuture<T> getOrWrap(ITaskContext context) {
        WorkerError.verify("Not able to wrap task").nonNull(context);
        futureMap.putIfAbsent(context.getKey(), new CompletableTaskFuture<>(context));
        return (CompletableTaskFuture<T>) futureMap.get(context.getKey());
    }

    @Override
    public void release(ITaskContext task) {
        // we cannot do this in completableFuture.whenComplete, because it can complete before we call waitUntilFinish, so we do a manual clean
        WorkerError.verify("Not able to wrap task").nonNull(task);
        futureMap.remove(task.getKey());
    }
}
