package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class StandardSyncWorkerPool<T extends AbstractTask, GroupByType> extends StandardWorkerPool<T> {

    protected final Map<GroupByType, ConcurrentLinkedQueue<T>> requestTaskMap = new ConcurrentHashMap<>();

    public StandardSyncWorkerPool(int maxWorker) {
        super(maxWorker);
    }

    public void enqueueAll(GroupByType groupBy, Collection<T> tasks) {
        if(!requestTaskMap.containsKey(groupBy)) {
            requestTaskMap.put(groupBy, new ConcurrentLinkedQueue<>());
        }
        if(tasks != null) {
            for (T t : tasks) {
                enqueue(t);
                requestTaskMap.get(groupBy).add(t);
            }
        }
    }

    public void waitUntilFinish(GroupByType groupBy){
        doWait(groupBy);
        requestTaskMap.remove(groupBy);
    }

    protected void doWait(GroupByType groupBy) {
        boolean await = true;
        while(await && requestTaskMap.containsKey(groupBy)) {
            await = false;
            for (T task : requestTaskMap.get(groupBy)) {
                if(task.isError()) {
                    throw new WorkerException("Task failed");
                }
                if(!task.isDone()){
                    await = true;
                    break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new WorkerException(e.getMessage(), e);
            }
        }

    }
}
