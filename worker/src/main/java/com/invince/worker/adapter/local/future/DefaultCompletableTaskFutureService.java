package com.invince.worker.adapter.local.future;

import com.invince.exception.WorkerError;
import com.invince.worker.core.ITaskIdentify;
import com.invince.worker.core.future.CompletableTaskFuture;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultCompletableTaskFutureService to help generate/simulate CompletableTaskFuture from a baseTask
 */
@Service
public class DefaultCompletableTaskFutureService implements ICompletableTaskFutureService {

    private static final ConcurrentHashMap<String, CompletableTaskFuture<?>> futureMap = new ConcurrentHashMap<>();

    /**
     * Either create or get (if already created) CompletableTaskFuture from task key + task prefix.
     *
     * @param context task key + task prefix
     * @param <SingleResult> resultType
     * @return CompletableTaskFuture
     */
    @Override
    public <SingleResult> CompletableTaskFuture<SingleResult> getOrWrap(ITaskIdentify context) {
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
    public void release(ITaskIdentify task) {
        // we cannot do this in completableFuture.whenComplete, because it can complete before we call waitUntilFinish, so we do a manual clean
        WorkerError.verify("Not able to wrap task").nonNull(task);
        futureMap.remove(task.getKey());
    }
}
