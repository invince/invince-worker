package com.invince.worker.core.monitor;

import com.invince.worker.core.StandardWorkerPool;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * workerPool status
 */
@Getter
@Setter
@Accessors(chain = true)
public class WorkerPoolStatus {

    private String name;
    private int toDoListSize;
    private int processingListSize;
    private int permanentWorkerLaunched;

    public WorkerPoolStatus(StandardWorkerPool<?> pool) {
        if (pool != null) {
            name = pool.getName();
            toDoListSize = pool.getToDoListSize();
            processingListSize = pool.getProcessingListSize();
            permanentWorkerLaunched = pool.getPermanentWorkerSize();
        }
    }
}
