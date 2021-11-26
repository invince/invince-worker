package com.invince.worker.collections.redis;

import com.invince.util.SafeRunner;
import com.invince.worker.BaseTask;
import com.invince.worker.FinishTask;
import com.invince.worker.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RedisTodoTasks implements IToDoTasks {

    private static final String TODO_LIST = "$TODO_LIST$";
    private static final String TODO_LIST_KEY = "$TODO_LIST_KEY$";
    private final RedissonClient redisson;

    private final String prefix;

    // You cannot put FinishTask into redis queue, you're not sure all worker takes it
    private final Mono<FinishTask> finish;
    private MonoSink<FinishTask> finishSink;

    public RedisTodoTasks(RedissonClient redisson, String prefix) {
        this.redisson = redisson;
        this.prefix = prefix;
        this.finish = Mono.create(sink -> finishSink = sink);
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
        return getRedisBQ().take();
    }

    @Override
    public boolean add(BaseTask task) {
        if (task instanceof FinishTask) {
            if (finishSink != null) {
                this.finishSink.success((FinishTask) task);
                return true;
            } else {
                log.debug("Fail to add finishTask, the blocking queue is not initialized correctly");
                return false;
            }
        } else {
            boolean rt = getRedisBQ().add(task);
            RList<String> todoKeyCopy = redisson.getList(prefix + TODO_LIST_KEY);
            todoKeyCopy.add(task.getKey());

            log.debug("Task {} add into redis todo blocking queue", task.getKey());
            return rt;
        }
    }

    @Override
    public void subscribe(Runnable finishCallback) {
        log.info("One worker subscribed");
        finish.subscribe(finishTask -> {
            SafeRunner.run(finishCallback);
        });
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
