package com.invince.worker.collections;

import com.invince.worker.BaseTask;

public interface IToDoTasks {

    int size();

    boolean exist(String key);

    BaseTask take() throws InterruptedException;

    boolean add(BaseTask task);

    void subscribe(Runnable onFinishCallBack);

    boolean moveToProcessing(String key);

    void cancel(String key);
}
