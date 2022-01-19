package com.invince.worker.core;

import com.invince.worker.adapter.local.collections.DefaultWorkerPoolHelper;
import com.invince.worker.core.collections.IWorkerPoolHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(chain = true)
@Setter
@Getter
public class WorkerPoolSetup {

    private String name = UUID.randomUUID().toString();

    private int maxNbWorker;

    private boolean isUnlimited = false;

    private boolean isLazyCreation = true;

    private IWorkerPoolHelper helper = new DefaultWorkerPoolHelper();
}
