package io.github.invince.worker.adapter.local.collections;

import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * DefaultToDoTasks is a LinkedBlockingQueue of task.
 * We put BaseTask because we need accept both task type of your workerPool and FinishTask
 */
@Slf4j
public class DefaultToDoTasks extends LinkedBlockingQueue<BaseTask> implements IToDoTasks {

    // because when you take the task from toDo queue, before you put it into processing list,
    // this task is neither in todo nor in processing list, we need keep it somewhere before we put it into processing list
    private final Set<String> toDoKeyCopy = ConcurrentHashMap.newKeySet();
    private final Set<String> keysToCancel = ConcurrentHashMap.newKeySet();


    /**
     * Check if task key exist in todo list
     * @param key task key
     * @return if task key exist in todo list
     */
    @Override
    public boolean exist(String key) {
        return key != null && toDoKeyCopy.contains(key);
    }


    /**
     * Task is moved to processing list, so we need remove the key from toDo list
     * @param key task key
     * @return if remove is successful
     */
    @Override
    public boolean movedToProcessing(String key) {
        return toDoKeyCopy.remove(key);
    }

    /**
     * Cancel a task in toDo list
     * @param key task key
     */
    @Override
    public void cancel(String key) {
        if (!StringUtils.isEmpty(key)) {
            keysToCancel.add(key);
        }
    }

    /**
     * Task a task, if it's in the keysToCancel list, we'll call cancelToDo, and later in worker, we won't process it, just throw ToDoTaskCancelled exception
     * @return the task
     * @throws InterruptedException
     */
    @Override
    public BaseTask take() throws InterruptedException {
        var task = super.take();
        if(task.getKey() != null && keysToCancel.contains(task.getKey())) {
            task.cancelToDo();
            keysToCancel.remove(task.getKey());
        }
        return task;
    }

    /**
     * Add new task in toDo list
     * @param baseTask the task
     * @return successful or not
     */
    @Override
    public boolean add(BaseTask baseTask) {
        boolean rt = super.add(baseTask);
        this.toDoKeyCopy.add(baseTask.getKey());
        return rt;
    }
}
