package com.invince.worker;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.invince.worker.collections.IProcessingTasks;
import com.invince.worker.collections.IToDoTasks;
import com.invince.worker.collections.IWorkerPoolHelper;
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

    protected IToDoTasks toDo;
    protected IProcessingTasks<String, T> processingTask;

    private final List<StandardWorker<T>> permanentWorkers = new ArrayList<>();
    private final List<OneshotWorker<T>> tempWorkers = new ArrayList<>();

    private final AtomicInteger permanentWorkerLaunched = new AtomicInteger(0);
    private final AtomicInteger tempWorkerLaunched = new AtomicInteger(0);

    private final ThreadPoolExecutor executor;

    protected final WorkerPoolSetup config;

    public StandardWorkerPool(WorkerPoolSetup config) {
        this.config = config;
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(getClass().getSimpleName() + "-thread-%d").build();
        if(config.isUnlimited()) {
            this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(namedThreadFactory);
        } else if (config.getMaxNbWorker() > 0){
            this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.getMaxNbWorker(), namedThreadFactory);
        } else {
            this.executor =  null;
        }
        init();
    }

    void init() {
        IWorkerPoolHelper ioc = config.getHelper();
        this.toDo = ioc.newToDoTasks(config.getName());
        this.processingTask = ioc.newProcessingTasks(config.getName());
        if(!config.isLazyCreation() && config.getMaxNbWorker() > 0) {
            for (int i = 0; i < config.getMaxNbWorker(); i++) {
                 newWorker();
            }
        }
    }

    public T enqueue(T task){
        if(task == null) {
            return null;
        }
        if(config.isUnlimited()) {
            newTempWorker();
        } else if(permanentWorkerLaunched.get() < config.getMaxNbWorker()) {
            newWorker();
        }
        task.onEnqueueSafe();
        if(!this.toDo.add(task)){
            log.error("Fail to add {} into to do list", task.getKey());
        }
        if(tempWorkerLaunched.get() > 0) {
            log.debug("{}'s todo list has {} tasks. {} temp worker started",
                    getClass().getSimpleName(), toDo.size(), tempWorkerLaunched.get());
        } else {
            log.debug("{}'s todo list has {} tasks. {} permanent worker started",
                    getClass().getSimpleName(), toDo.size(), permanentWorkerLaunched.get());
        }
        return task;
    }


    public boolean existTodoTask(String key) {
        return toDo != null && toDo.exist(key);
    }

    public boolean existProcessingTask(String key) {
        return processingTask != null && processingTask.exist(key);
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
        if(config.isUnlimited()) {
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

}
