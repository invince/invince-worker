package io.github.invince.worker.core.monitor;

/**
 * DefaultWorkerPoolMonitorService to have WorkerPoolStatus
 * Note: you can group list of workerPool.
 *       For ex, if you have a complex process using 3 pool, put same group name on these 3 pools
 *               and when you do IWorkerPoolMonitorService.get(groupName), you got status of all the 3 pools
 */
public interface IWorkerPoolMonitorService {

    /**
     * Get status of all workerPool in same group
     * @param groupName group name, cf IWorkerPool.getGroupName
     * @return status of all workerPool in same group
     */
    WorkerPoolGroupStatus get(String groupName);
}
