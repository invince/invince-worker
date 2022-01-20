package com.invince.worker.core.collections;

import com.invince.worker.core.BaseTask;

public interface IToDoTasks {

    int size();

    boolean exist(String key);

    BaseTask take() throws InterruptedException;

    boolean add(BaseTask task);

    void subscribe(Runnable onFinishCallBack);

    boolean movedToProcessing(String key);

    void cancel(String key);

    default void close() {}
}
