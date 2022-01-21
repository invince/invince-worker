package io.github.invince.worker.core;

import io.github.invince.worker.adapter.local.helper.DefaultWorkerPoolHelper;
import io.github.invince.worker.core.helper.IWorkerPoolHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * The workerPool setup
 */
@Accessors(chain = true)
@Setter
@Getter
public class WorkerPoolSetup {

    private String queueName = UUID.randomUUID().toString();

    private int maxNbWorker;

    private boolean isUnlimited = false;

    // NOTE: by default worker will be created each time we enqueue a task until reach the maxNbWorker.
    // in distributed mode (for ex redis mode), we need create all the workers first, because we may enqueue task on other node
    private boolean isLazyCreation = true;

    // for each mode, you need implements and set your helper (to tell workerPool how to create toDo, processing list, taskGroup and how to bind a task to a CompletableTaskFuture)
    private IWorkerPoolHelper helper = new DefaultWorkerPoolHelper();
}
