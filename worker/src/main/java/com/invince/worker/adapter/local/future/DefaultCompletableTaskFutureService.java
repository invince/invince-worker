package com.invince.worker.adapter.local.future;

import com.invince.exception.WorkerError;
import com.invince.worker.core.ITaskContext;
import com.invince.worker.core.future.CompletableTaskFuture;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultCompletableTaskFuture is CompletableTaskFuture which is a standard java CompletableFuture + task key and task prefix
 */
@Service
public class DefaultCompletableTaskFutureService implements ICompletableTaskFutureService {

    private static final ConcurrentHashMap<String, CompletableTaskFuture<?>> futureMap = new ConcurrentHashMap<>();

    /**
     * From task key + task prefix, how to create a CompletableTaskFuture.
     * And make sure it's unique and we can find it later by task key + task prefix
     *
     * @param context task key + task prefix
     * @param <SingleResult> resultType
     * @return
     */
    @Override
    public <SingleResult> CompletableTaskFuture<SingleResult> getOrWrap(ITaskContext context) {
        WorkerError.verify("Not able to wrap task").nonNull(context);
        futureMap.putIfAbsent(context.getKey(), new CompletableTaskFuture<>(context));
        return (CompletableTaskFuture<SingleResult>) futureMap.get(context.getKey());
    }

    /**
     * At the end of a task, we can release it to free memory usage
     *
     * @param task task key + task prefix
     */
    @Override
    public void release(ITaskContext task) {
        // we cannot do this in completableFuture.whenComplete, because it can complete before we call waitUntilFinish, so we do a manual clean
        WorkerError.verify("Not able to wrap task").nonNull(task);
        futureMap.remove(task.getKey());
    }
}
