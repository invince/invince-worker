package com.invince.worker.core.monitor;

public interface IWorkerPoolMonitorService {

    WorkerPoolGroupStatus get(String groupName);
}
