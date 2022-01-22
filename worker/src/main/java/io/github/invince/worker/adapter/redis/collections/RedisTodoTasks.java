package io.github.invince.worker.adapter.redis.collections;

import io.github.invince.util.SafeRunner;
import io.github.invince.worker.core.BaseTask;
import io.github.invince.worker.core.FinishTask;
import io.github.invince.worker.core.collections.IToDoTasks;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RedisTodoTasks implements IToDoTasks {

    private static final String TODO_LIST = "$TODO_LIST$";
    private static final String TODO_LIST_KEY = "$TODO_LIST_KEY$";
    private static final String KEYS_TO_CANCEL = "$KEYS_TO_CANCEL$";
    private final RedissonClient redisson;

    private final String prefix;

    /**
     * 1. to reduce the usage of redis RBlockingQueue, we create a thread take element from that queue and put into this local BlockingQueue which be used by all workers
     * 2. we need listen to StandardTask from redis RBlockingQueue + FinishTask handled only in local, that's why we create this combined queue
      */
    private final LinkedBlockingQueue<BaseTask> blockingQueueLocal = new LinkedBlockingQueue<>();

    private final AtomicBoolean listenRunning = new AtomicBoolean(false);
    private ExecutorService listenerExecutor;

    public RedisTodoTasks(RedissonClient redisson, String prefix) {
        this.redisson = redisson;
        this.prefix = prefix;
    }

    /**
     * Start listen to the todo list, if nbWorker = 0, we don't start it
     */
    @Override
    public void startListening() {
        listenToRBlockingQueue();
    }

    /**
     *
     * @return size of the toDo list, we don't need check finish task, so we check directly redis queue
     */
    @Override
    public int size() {
        return getRedisBQ().size();
    }

    /**
     *
     * @param key task key
     * @return check if task key exists in toDo list, we don't need check finish task, so we check directly in redis queue
     */
    @Override
    public boolean exist(String key) {
        return !StringUtils.hasText(key) && getRedisBQ().stream().anyMatch(one -> key.equals(one.getKey()));
    }

    /**
     *
     * @return take element from blockingQueueLocal
     * @throws InterruptedException InterruptedException
     */
    @Override
    public BaseTask take() throws InterruptedException {
        return blockingQueueLocal.take();
    }

    @Override
    public void cancel(String key) {
        if (!StringUtils.hasText(key)) {
            redisson.getList(prefix + KEYS_TO_CANCEL).add(key);
        }
    }

    @Override
    public boolean add(BaseTask task) {
        if (task instanceof FinishTask) {
            return blockingQueueLocal.add(task);
        } else {
            boolean rt = getRedisBQ().add(task);
            RList<String> todoKeyCopy = redisson.getList(prefix + TODO_LIST_KEY);
            todoKeyCopy.add(task.getKey());

            log.debug("Task {} add into redis todo blocking queue", task.getKey());
            return rt;
        }
    }

    // reduce usage of redis, otherwise every worker do RBlockingQueue.take
    private void listenToRBlockingQueue() {
        if(!listenRunning.get()) {
            listenRunning.set(true);
            listenerExecutor = Executors.newSingleThreadExecutor();
            listenerExecutor.execute(() -> {
                try {
                    BaseTask task;
                    do {
                        try {
                            task = getRedisBQ().take(); // we don't use RBlockingQueue.subscribeOnElements because in that way, we limit the way to implement the fn using in that lambda, for ex task.cancelToDo you need do it in async way
                            if (task != null) { // normally impossible null
                                if (redisson.getList(prefix + KEYS_TO_CANCEL).contains(task.getKey())) {
                                    task.cancelToDo();
                                    redisson.getList(prefix + KEYS_TO_CANCEL).remove(task.getKey());
                                }
                                blockingQueueLocal.add(task);
                            }
                        } catch (Exception e) {
                            if (e instanceof InterruptedException) {
                                throw e;
                            }
                            log.error(e.getMessage(), e);
                        }
                    } while (listenRunning.get());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    @Override
    public boolean movedToProcessing(String key) {
        RList<String> todoKeyCopy = redisson.getList(prefix + TODO_LIST_KEY);
        return todoKeyCopy.remove(key);
    }

    @Override
    public void close() {
        if (listenRunning.get() && listenerExecutor != null) {
            listenRunning.set(false);
            SafeRunner.run(listenerExecutor::shutdown);
        }
    }

    private RBlockingQueue<BaseTask> getRedisBQ() {
        return redisson.getBlockingQueue(prefix + TODO_LIST);
    }
}
