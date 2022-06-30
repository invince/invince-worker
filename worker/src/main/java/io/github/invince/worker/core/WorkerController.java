package io.github.invince.worker.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WorkerController<T extends BaseTask> {

    @Getter
    private final List<StandardWorker<T>> permanentWorkers = new ArrayList<>();
    @Getter
    private final List<OneshotWorker<T>> tempWorkers = new ArrayList<>();
    @Getter
    private final AtomicInteger permanentWorkerLaunched = new AtomicInteger(0);
    @Getter
    private final AtomicInteger tempWorkerLaunched = new AtomicInteger(0);
    private final WorkerPoolSetup config;
    private final ThreadPoolExecutor executor;


    public WorkerController(WorkerPoolSetup config) {
        this.config = config;
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(getClass().getSimpleName() + "-thread-%d").build();
        if(config.isUnlimited()) {
            this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(namedThreadFactory);
        } else if (config.getMaxNbWorker() > 0){
            this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.getMaxNbWorker(), namedThreadFactory);
        } else {
            this.executor = null; // for a front node maybe
        }
    }

    void createNewWorkerIfNecessary(ICompletableTaskFutureService completableTaskFutureService, IToDoTasks toDo, IProcessingTasks<String, T> processing) {
        if(config.isUnlimited()) {
            newTempWorker(completableTaskFutureService, toDo, processing);
        } else if(permanentWorkerLaunched.get() < config.getMaxNbWorker()) {
            newPermanentWorker(completableTaskFutureService, toDo, processing);
        }
    }

    void newPermanentWorker(ICompletableTaskFutureService completableTaskFutureService, IToDoTasks toDo, IProcessingTasks<String,T> processing) {
        StandardWorker<T> worker = new StandardWorker<>(completableTaskFutureService, toDo, processing);
        toDo.startListening();
        permanentWorkers.add(worker);
        executor.execute(worker);
        log.debug("{} new worker created", getClass().getSimpleName());
        permanentWorkerLaunched.incrementAndGet();
    }

    void newTempWorker(ICompletableTaskFutureService completableTaskFutureService, IToDoTasks toDo, IProcessingTasks<String, T> processing) {
        OneshotWorker<T> worker = new OneshotWorker<>(completableTaskFutureService, toDo, processing);
        toDo.startListening();
        tempWorkers.add(worker);
        executor.execute(worker);
        log.debug("{} new temp worker created", getClass().getSimpleName());
        tempWorkerLaunched.incrementAndGet();
    }

    void shutdown(boolean await) {
        if(await) {
            if(config.isUnlimited()) {
                CompletableFuture.allOf(tempWorkers.toArray(new OneshotWorker[0])).join();
            } else {
                CompletableFuture.allOf(permanentWorkers.toArray(new StandardWorker[0])).join();
            }
        }

        if(executor != null) {
            this.executor.shutdown();
        }
    }

    /**
     *
     * @return true if we're in unlimited mode or still more than one worker is available
     */
    public boolean hasAvailableWorker() {
        return this.config.isUnlimited() || permanentWorkers.stream().anyMatch(StandardWorker::isAvailable);
    }
}
