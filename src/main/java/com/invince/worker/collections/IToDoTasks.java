package com.invince.worker.collections;

import com.invince.worker.BaseTask;

public interface IToDoTasks {

    int size();

    boolean exist(String key);

    BaseTask take() throws InterruptedException;

    boolean add(BaseTask task);

    void subscribe();

    boolean movedToProcess(String key);
}
