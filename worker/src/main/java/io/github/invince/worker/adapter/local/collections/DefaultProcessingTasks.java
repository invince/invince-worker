package io.github.invince.worker.adapter.local.collections;

import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.collections.IProcessingTasks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * DefaultProcessingTasks is a ConcurrentHashMap of taskKey to task
 *
 * @param <K> task key (we can use only task key, not the task uniqueKey, because each workerPool shall have its own IProcessingTasks)
 * @param <V> task type
 */
@Slf4j
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


    /**
     * (In distributed mode), if your task is processed by a worker node, and that node crashes,
     * we shall be able to restore it and put it back to todo list
     * @param key task key
     * @param consumer consumer to rescue the task
     * @return success or not
     */
    @Override
    public boolean tryRestoreCrashedProcessingTask(K key, Consumer<V> consumer) {
        log.debug("In local mode, there is nothing to do when the node itself crashes");
        return false;
    }
}
