package com.invince.worker.collections;

import com.invince.worker.BaseTask;

public interface IProcessingTasks<K, V extends BaseTask> {

    V put(K key, V value);

    V remove(Object key);

    boolean exist(K key);

    int size();

    void cancel(K key);
}
