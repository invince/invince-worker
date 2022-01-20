package com.invince.worker.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.invince.util.SafeRunner;
import com.invince.worker.core.collections.IProcessingTasks;
import com.invince.worker.core.collections.IToDoTasks;
import com.invince.worker.core.collections.IWorkerPoolHelper;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
public class StandardWorkerPool<T extends BaseTask> implements IWorkerPool<T>  {

    protected IToDoTasks toDo;
    protected IProcessingTasks<String, T> processingTasks;
    protected ICompletableTaskFutureService completableTaskFutureService;

    private final List<StandardWorker<T>> permanentWorkers = new ArrayList<>();
    private final List<OneshotWorker<T>> tempWorkers = new ArrayList<>();

    private final AtomicInteger permanentWorkerLaunched = new AtomicInteger(0);
    private final AtomicInteger tempWorkerLaunched = new AtomicInteger(0);

    private final ThreadPoolExecutor executor;

    protected final WorkerPoolSetup config;
    protected final String poolUid;


    public StandardWorkerPool(WorkerPoolSetup config) {
        poolUid = getName() + "-" + UUID.randomUUID();
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

    private void init() {
        beforeInit();
        IWorkerPoolHelper ioc = config.getHelper();
        this.toDo = ioc.newToDoTasks(config.getName());
        this.processingTasks = ioc.newProcessingTasks(config.getName(), poolUid);
        this.completableTaskFutureService = ioc.getCompletableTaskFutureService();
        if(!config.isLazyCreation() && config.getMaxNbWorker() > 0) {
            for (int i = 0; i < config.getMaxNbWorker(); i++) {
                 newWorker();
            }
        }
        afterInit();
    }

    @Override
    public int getToDoListSize() {
        return toDo != null ? toDo.size() : 0;
    }

    @Override
    public int getProcessingListSize() {
        return processingTasks != null ? processingTasks.size() : 0;
    }

    @Override
    public int getPermanentWorkerSize() {
        return permanentWorkers.size();
    }

    @Override
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


    @Override
    public boolean existToDoTask(String key) {
        return toDo != null && toDo.exist(key);
    }

    @Override
    public boolean existProcessingTask(String key) {
        return processingTasks != null && processingTasks.exist(key);
    }

    @Override
    public T removeTask(String key){
        return processingTasks.remove(key);
    }

    @Override
    public void cancelTask(String key) {
        boolean cancelled = false;
        if (toDo.exist(key)) {
            log.debug("Task {} in todo list to be cancelled", key);
            toDo.cancel(key);
            cancelled = true;
        }
        if (processingTasks.exist(key)) {
            log.debug("Task {} in progress to be cancelled", key);
            processingTasks.cancel(key);
            cancelled = true;
        }
        if(!cancelled){
            log.debug("Task {} is neither in toDo list, nor in progress, cannot cancel it", key);
        }
    }

    private void newWorker() {
        StandardWorker<T> worker = new StandardWorker<>(completableTaskFutureService, toDo, processingTasks);
        permanentWorkers.add(worker);
        executor.execute(worker);
        log.debug("{} new worker created", getClass().getSimpleName());
        permanentWorkerLaunched.incrementAndGet();
    }

    private void newTempWorker() {
        OneshotWorker<T> worker = new OneshotWorker<>(completableTaskFutureService, toDo, processingTasks);
        tempWorkers.add(worker);
        executor.execute(worker);
        log.debug("{} new temp worker created", getClass().getSimpleName());
        tempWorkerLaunched.incrementAndGet();
    }

    @Override
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
        SafeRunner.run(toDo::close);
        SafeRunner.run(processingTasks::close);

        if(executor != null) {
            this.executor.shutdown();
        }
        log.info("[StandardWorkerPool]: All task has been processed.");
    }

    protected void beforeInit() {
    }

    protected void afterInit() {
    }
}
