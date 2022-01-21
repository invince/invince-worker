package com.invince.worker.core.monitor;

import com.invince.exception.WorkerError;
import com.invince.worker.core.StandardWorkerPool;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * helper to get the latest workerPool status
 */
@Data
@AllArgsConstructor
public class WorkerPoolStatusBuilder implements Comparable<WorkerPoolStatusBuilder> {

    private StandardWorkerPool<?> pool;

    public WorkerPoolStatus build() {
        return new WorkerPoolStatus(pool);
    }

    @Override
    public int compareTo(WorkerPoolStatusBuilder builder) {
        WorkerError.verify("Null pool")
                .nonNull(pool, builder, pool.getName(), builder.getPool(), builder.getPool().getName());
        return - pool.getName().compareTo(builder.getPool().getName());
    }
}
