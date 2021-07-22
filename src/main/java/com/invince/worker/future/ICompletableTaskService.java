package com.invince.worker.future;

import com.invince.worker.BaseTask;

import java.util.concurrent.CompletableFuture;

public interface ICompletableTaskService {
    <T> CompletableFuture<T> getOrWrap(BaseTask tBaseTask);
}
