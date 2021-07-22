package com.invince.worker.collections.local;

import com.invince.worker.BaseTask;
import com.invince.worker.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class DefaultToDoTasks extends LinkedBlockingQueue<BaseTask> implements IToDoTasks {

    private Set<String> toDoKeyCopy = ConcurrentHashMap.newKeySet(); // because there is a gap between we take from toDo list and put it into processing. during the gap task is neither in todo nor in processing

    @Override
    public boolean exist(String key) {
        return key != null && toDoKeyCopy.contains(key);
    }

    @Override
    public void subscribe() {
        log.info("One Worker subscribed");
    }

    @Override
    public boolean movedToProcess(String key) {
        return toDoKeyCopy.remove(key);
    }

    @Override
    public boolean add(BaseTask baseTask) {
        boolean rt = super.add(baseTask);
        this.toDoKeyCopy.add(baseTask.getKey());
        return rt;
    }
}
