package com.invince.worker.collections.redis;

import com.invince.worker.BaseTask;
import com.invince.worker.FinishTask;
import com.invince.worker.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RedisTodoTasks implements IToDoTasks {

    private static final String TODO_LIST = "$TODO_LIST$";
    private static final String TODO_LIST_KEY = "$TODO_LIST_KEY$";
    private final RedissonClient redisson;

    private final String prefix;

    private AtomicBoolean subscribed = new AtomicBoolean(false);

    // because we need handle FinishTask only in local, but receive also task from redis

    private BlockingQueue<BaseTask> combinedBQ = new LinkedBlockingQueue<>();

    public RedisTodoTasks(RedissonClient redisson, String prefix) {
        this.redisson = redisson;
        this.prefix = prefix;
    }


    @Override
    public int size() {
        return getRedisBQ().size();
    }

    @Override
    public boolean exist(String key) {
        return !StringUtils.isEmpty(key) && getRedisBQ().stream().anyMatch(one -> key.equals(one.getKey()));
    }

    @Override
    public BaseTask take() throws InterruptedException {
        return combinedBQ.take();
    }

    @Override
    public boolean add(BaseTask task) {
        if(task instanceof FinishTask) {
            return combinedBQ.add(task);
        } else {
            boolean rt = getRedisBQ().add(task);
            RList<String> todoKeyCopy = redisson.getList(prefix + TODO_LIST_KEY);
            todoKeyCopy.add(task.getKey());

            log.debug("Task {} add into redis todo blocking queue", task.getKey());
            return rt;
        }
    }

    @Override
    public void subscribe() {
        log.info("One worker subscribed");
        if(!subscribed.get()){
            getRedisBQ().subscribeOnElements(task -> { // only one receive this
                log.debug("Task {} taken from redis todo blocking queue", task.getKey());
                combinedBQ.add(task);
            });
            subscribed.set(true);
        }

    }

    @Override
    public boolean movedToProcess(String key) {
        RList<String> todoKeyCopy = redisson.getList(prefix + TODO_LIST_KEY);
        return todoKeyCopy.remove(key);
    }

    private RBlockingQueue<BaseTask> getRedisBQ() {
        return redisson.getBlockingQueue(prefix + TODO_LIST);
    }
}
