package com.invince.worker.collections.local;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IProcessingTasks;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultProcessingTasks<K, V extends BaseTask> extends ConcurrentHashMap<K, V>
        implements IProcessingTasks<K, V> {


    @Override
    public boolean exist(K key) {
        return key != null && containsKey(key);
    }

    @Override
    public void cancel(K key) {
        if (!StringUtils.isEmpty(key)) {
            var task = get(key);
            if (task != null) {
                task.cancelProcessing();
            }
        }
    }
}