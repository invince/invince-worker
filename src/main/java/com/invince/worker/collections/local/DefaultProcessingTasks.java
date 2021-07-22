package com.invince.worker.collections.local;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IProcessingTasks;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultProcessingTasks<K, V extends BaseTask> extends ConcurrentHashMap<K,V> implements IProcessingTasks<K,V> {


    @Override
    public boolean exist(K key) {
        return key != null && containsKey(key);
    }
}
