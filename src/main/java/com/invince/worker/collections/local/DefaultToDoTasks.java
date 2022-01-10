package com.invince.worker.collections.local;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class DefaultToDoTasks extends LinkedBlockingQueue<BaseTask> implements IToDoTasks {

    private Set<String> toDoKeyCopy = ConcurrentHashMap.newKeySet(); // because there is a gap between we take from toDo list and put it into processing. during the gap task is neither in todo nor in processing
    private Set<String> keysToCancel = ConcurrentHashMap.newKeySet();


    @Override
    public boolean exist(String key) {
        return key != null && toDoKeyCopy.contains(key);
    }

    @Override
    public void subscribe(Runnable onFinishCallBack) {
        log.info("One Worker subscribed");
    }

    @Override
    public boolean moveToProcessing(String key) {
        return toDoKeyCopy.remove(key);
    }

    @Override
    public void cancel(String key) {
        if (!StringUtils.isEmpty(key)) {
            keysToCancel.add(key);
        }
    }

    @Override
    public BaseTask take() throws InterruptedException {
        var task = super.take();
        if(keysToCancel.contains(task.getKey())) {
            task.cancelToDo();
            keysToCancel.remove(task.getKey());
        }
        return task;
    }

    @Override
    public boolean add(BaseTask baseTask) {
        boolean rt = super.add(baseTask);
        this.toDoKeyCopy.add(baseTask.getKey());
        return rt;
    }
}
