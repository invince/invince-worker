package com.invince.worker;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class StandardWorkerPool<T extends BaseTask> implements DisposableBean {

    private final int maxWorker;
    private final AtomicInteger workerLaunched = new AtomicInteger(0);

    private final ThreadPoolExecutor executor;
    private final BlockingQueue<BaseTask> toDo;
    private final ConcurrentHashMap<String, T> processing = new ConcurrentHashMap<>();

    private List<StandardWorker<T>> workers = new ArrayList<>();


    public StandardWorkerPool(int maxWorker) {
        if(maxWorker <= 0) {
            maxWorker = 1;
        }
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(getClass().getSimpleName() + "-thread-%d").build();
        this.maxWorker = maxWorker;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxWorker, threadFactory);
        this.toDo = new LinkedBlockingDeque<>();
    }

    public T enqueue(T task){
        if(workerLaunched.get() < maxWorker) {
            newWorker();
            workerLaunched.incrementAndGet();
        }
        this.toDo.add(task);
        return task;
    }

    public boolean containsTask(String key) {
        return processing.containsKey(key);
    }

    public T removeTask(String key){
        return processing.remove(key);
    }

    private void newWorker() {
        StandardWorker<T> worker = new StandardWorker<>(toDo, processing);
        workers.add(worker);
        executor.execute(worker);
    }

    public void shutdown(boolean await) {
        for (int i = 0; i < maxWorker; i++) {
             toDo.add(new FinishTask());
        }
        CompletableFuture.allOf(workers.toArray(new StandardWorker[0])).join();
    }


    @Override
    public void destroy() throws Exception {
        if(this.executor != null){
            this.executor.shutdown();
        }
    }
}
