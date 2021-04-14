package com.invince.worker;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.invince.worker.collections.SubscribableBlockingQueue;
import com.invince.worker.collections.SubscribableConcurrentMap;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Do not forget to call shutdown when workerpool ends
 * @param <T>
 */
@Slf4j
public class StandardWorkerPool<T extends BaseTask>  {

    private final SubscribableBlockingQueue<BaseTask> toDo = new SubscribableBlockingQueue<>();
    private final SubscribableConcurrentMap<String, T> processingTask = new SubscribableConcurrentMap<>();

    private final List<StandardWorker<T>> permanentWorkers = new ArrayList<>();
    private final List<OneshotWorker<T>> tempWorkers = new ArrayList<>();

    private final boolean unlimited;
    private final int maxWorker;
    private final AtomicInteger permanentWorkerLaunched = new AtomicInteger(0);
    private final AtomicInteger tempWorkerLaunched = new AtomicInteger(0);

    private final ThreadPoolExecutor executor;

    public StandardWorkerPool(int maxWorker) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(getClass().getSimpleName() + "-thread-%d").build();
        unlimited = maxWorker <= 0;
        if(unlimited) {
            this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(namedThreadFactory);
        } else {
            this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxWorker, namedThreadFactory);
        }
        this.maxWorker = maxWorker;
    }

    public T enqueue(T task){
        if(unlimited) {
            newTempWorker();
        } else if(permanentWorkerLaunched.get() < maxWorker) {
            newWorker();
        }
        this.toDo.add(task);
        if(tempWorkerLaunched.get() > 0) {
            log.debug("{}'s todo list has {} tasks. {} temp worker started",
                    getClass().getSimpleName(), toDo.stream(), tempWorkerLaunched.get());
        } else {
            log.debug("{}'s todo list has {} tasks. {} permanent worker started",
                    getClass().getSimpleName(), toDo.stream(), permanentWorkerLaunched.get());
        }
        return task;
    }


    public boolean existTodoTask(String key) {
        return key != null && toDo.stream().anyMatch(one -> key.equals(one.getKey()));
    }

    public boolean existProcessingTask(String key) {
        return processingTask.containsKey(key);
    }

    public T removeTask(String key){
        return processingTask.remove(key);
    }

    private void newWorker() {
        StandardWorker<T> worker = new StandardWorker<>(toDo, processingTask);
        permanentWorkers.add(worker);
        executor.execute(worker);
        log.debug("{} new worker created", getClass().getSimpleName());
        permanentWorkerLaunched.incrementAndGet();
    }

    private void newTempWorker() {
        OneshotWorker<T> worker = new OneshotWorker<>(toDo, processingTask);
        tempWorkers.add(worker);
        executor.execute(worker);
        log.debug("{} new temp worker created", getClass().getSimpleName());
        tempWorkerLaunched.incrementAndGet();
    }

    public void shutdown(boolean await) {
        if(unlimited) {
            for (int i = 0; i < tempWorkers.size() * 2; i++) { // *2 to make sure
                toDo.add(new FinishTask());
            }
            if(await) {
                CompletableFuture.allOf(tempWorkers.toArray(new OneshotWorker[0])).join();
            }
        } else {
            for (int i = 0; i < permanentWorkers.size() * 2; i++) { // *2 to make sure
                toDo.add(new FinishTask());
            }
            if(await) {
                CompletableFuture.allOf(permanentWorkers.toArray(new StandardWorker[0])).join();
            }
        }
        if(executor != null) {
            this.executor.shutdown();
        }
        log.info("[StandardWorkerPool]: All task has been processed.");
    }

    /**
     * You can override this
     * @param work
     */
    protected void putIntoTodo(T work) {
        this.toDo.add(work);
    }
}
