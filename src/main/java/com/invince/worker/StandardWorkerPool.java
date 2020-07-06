package com.invince.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class StandardWorkerPool<T extends AbstractTask> implements DisposableBean {

    private final int maxWorker;
    private final AtomicInteger workerLaunched = new AtomicInteger(0);

    private final ThreadPoolExecutor executor;
    private final BlockingQueue<AbstractTask> toDo;
    private final ConcurrentHashMap<String, T> processing = new ConcurrentHashMap<>();
    private List<StandardWorker<T>> workers = new ArrayList<>();

    public StandardWorkerPool(int maxWorker) {
        if(maxWorker <= 0) {
            maxWorker = 1;
        }
        this.maxWorker = maxWorker;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxWorker);
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
        while(await) {
            await = false;
            for (StandardWorker<T> worker : workers) {
                if(!worker.finished()){
                    await = true;
                    break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                new WorkerException(e.getMessage(), e);
            }
        }
    }


    @Override
    public void destroy() throws Exception {
        if(this.executor != null){
            this.executor.shutdown();
        }
    }
}
