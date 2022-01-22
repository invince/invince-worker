package io.github.invince.worker.adapter.local.collections;

import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.collections.IProcessingTasks;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultProcessingTasks is a ConcurrentHashMap of taskKey to task
 *
 * @param <K> task key (we can use only task key, not the task uniqueKey, because each workerPool shall have its own IProcessingTasks)
 * @param <V> task type
 */
public class DefaultProcessingTasks<K, V extends BaseTask> extends ConcurrentHashMap<K, V>
        implements IProcessingTasks<K, V> {


    /**
     * Check if task key exist in processing list
     * @param key task key
     * @return if task key exist in processing list
     */
    @Override
    public boolean exist(K key) {
        return key != null && containsKey(key);
    }

    /**
     * Cancel a task via task key
     * @param key task key
     */
    @Override
    public void cancel(String key) {
        if (!StringUtils.hasText(key)) {
            var task = get(key);
            if (task != null) {
                task.cancelProcessing();
            }
        }
    }
}
