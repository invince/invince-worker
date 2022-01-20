package com.invince.worker.core.collections;

import com.invince.worker.core.BaseTask;

public interface IProcessingTasks<K, V extends BaseTask> {

    V put(K key, V value);

    V remove(Object key);

    boolean exist(K key);

    int size();

    void cancel(String key);

    default void close() {}
}
