package io.github.invince.worker.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.invince.util.SafeRunner;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.core.helper.IWorkerPoolHelper;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.Getter;
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
 * Blocking queue style workerPool
 * NOTE: Do not forget to call shutdown when workerpool ends
 * @param <T> task type
 */
@Slf4j
public class StandardWorkerPool<T extends BaseTask> implements IWorkerPool<T>  {

    protected IToDoTasks toDo;
    protected IProcessingTasks<String, T> processingTasks;
    protected ICompletableTaskFutureService completableTaskFutureService;


    protected final WorkerController<T> workerController;
    @Getter
    protected final WorkerPoolSetup config;
    protected final String poolUid;


    public StandardWorkerPool(WorkerPoolSetup config) {
        poolUid = getName() + "-" + UUID.randomUUID();
        this.config = config;
        this.workerController = new WorkerController<>(config);
        beforeInit();
        init();
        afterInit();
    }

    private void init() {
        IWorkerPoolHelper ioc = config.getHelper();
        this.toDo = ioc.newToDoTasks(config, workerController, poolUid);
        this.processingTasks = ioc.newProcessingTasks(config, workerController, poolUid);
        this.completableTaskFutureService = ioc.getCompletableTaskFutureService();
        if(!config.isLazyCreation() && config.getMaxNbWorker() > 0) {
            for (int i = 0; i < config.getMaxNbWorker(); i++) {
                 workerController.newPermanentWorker(completableTaskFutureService, toDo, processingTasks);
            }
        }
    }

    /**
     *
     * @return todo list size, used in monitor service
     */
    @Override
    public int getToDoListSize() {
        return toDo != null ? toDo.size() : 0;
    }

    /**
     *
     * @return processing list size, used in monitor service
     */
    @Override
    public int getProcessingListSize() {
        return processingTasks != null ? processingTasks.size() : 0;
    }

    /**
     *
     * @return nb of permanent worker started, used in monitor service
     */
    @Override
    public int getPermanentWorkerSize() {
        return workerController.getPermanentWorkers().size();
    }

    /**
     * Enqueue a task in the workpool
     * - in unlimited mode, each time we create a new OneshotWorker
     * - in lazyCreation (with a maxNbWorker) mode (default one), if nb of worker doesn't reach the maxNbWorker, we'll create a new worker
     * - in not lazyCreation mode, all workers should be created in constructor
     * @param task task
     * @return the task
     */
    @Override
    public T enqueue(T task){
        if(task == null) {
            return null;
        }
        this.workerController.createNewWorkerIfNecessary(completableTaskFutureService, toDo, processingTasks);
        task.onEnqueueSafe();

        if(config.getMaxRetryAttempts() > 0) {
            task.setRetryChances(config.getMaxRetryAttempts());
        }

        if(!this.toDo.add(task)){
            log.error("Fail to add {} into to do list", task.getKey());
        }
        if(workerController.getTempWorkerLaunched().get() > 0) {
            log.debug("{}'s todo list has {} tasks. {} temp worker started",
                    getClass().getSimpleName(), toDo.size(), workerController.getTempWorkerLaunched().get());
        } else {
            log.debug("{}'s todo list has {} tasks. {} permanent worker started",
                    getClass().getSimpleName(), toDo.size(), workerController.getPermanentWorkerLaunched().get());
        }
        return task;
    }

    /**
     *
     * @param key task key
     * @return if task exists in todo list
     */
    @Override
    public boolean existToDoTask(String key) {
        return toDo != null && toDo.exist(key);
    }

    /**
     *
     * @param key task key
     * @return if task exists in processing list
     */
    @Override
    public boolean existProcessingTask(String key) {
        return processingTasks != null && processingTasks.exist(key);
    }

    /**
     * remove the task via
     * @param key task key
     * @return the removed task
     */
    @Override
    public T removeTask(String key){
        return processingTasks.remove(key);
    }

    /**
     * cancel a task (no matter it's in todo list or processing list)
     * @param key task list
     */
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

    /**
     * (In distributed mode), if your task is processed by a worker node, and that node crashes,
     * we shall be able to restore it and put it back to todo list
     * @param key task key
     * @return success or not
     */
    @Override
    public boolean tryRestoreCrashedProcessingTask(String key) {
        log.debug("Rescuing {}", key);
        return processingTasks != null && toDo != null
                && processingTasks.tryRestoreCrashedProcessingTask(key, toDo::add);
    }

    /**
     * shutdown the workpool.
     * - we will add enough FinishTask into todo list to ends all worker
     * - close the todo and processing (you can implement necessary thing for your custom type of todo/processing list)
     * - shutdown the main executor
     * @param await if we wait current process finish
     */
    @Override
    public void shutdown(boolean await) {
        if(config.isUnlimited()) {
            for (int i = 0; i < workerController.getTempWorkerLaunched().get() * 2; i++) { // *2 to make sure
                toDo.add(new FinishTask());
            }
        } else {
            for (int i = 0; i < config.getMaxNbWorker() * 2; i++) { // *2 to make sure
                toDo.add(new FinishTask());
            }
        }
        SafeRunner.run(() -> workerController.shutdown(await));
        SafeRunner.run(toDo::close);
        SafeRunner.run(processingTasks::close);

        log.info("[StandardWorkerPool]: All task has been processed.");
    }

    /**
     * called before init() in constructor
     */
    protected void beforeInit() {
    }

    /**
     * called after init() in constructor
     */
    protected void afterInit() {
    }
}
