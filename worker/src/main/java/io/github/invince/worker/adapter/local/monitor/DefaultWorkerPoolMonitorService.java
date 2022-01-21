package io.github.invince.worker.adapter.local.monitor;

import io.github.invince.worker.core.StandardWorkerPool;
import io.github.invince.worker.core.monitor.IWorkerPoolMonitorService;
import io.github.invince.worker.core.monitor.WorkerPoolGroupStatus;
import io.github.invince.worker.core.monitor.WorkerPoolStatusBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DefaultWorkerPoolMonitorService to have WorkerPoolStatus
 * Note: you can group list of workerPool.
 *       For ex, if you have a complex process using 3 pool, put same group name on these 3 pools
 *               and when you do IWorkerPoolMonitorService.get(groupName), you got status of all the 3 pools
 */
@Service
public class DefaultWorkerPoolMonitorService implements IWorkerPoolMonitorService {

    private final Map<String, List<WorkerPoolStatusBuilder>> db = new HashMap<>();

    @Autowired
    public DefaultWorkerPoolMonitorService(List<StandardWorkerPool<?>> pools) {
        if (pools != null) {
            pools.stream()
                    .filter(one -> one != null
                            && !StringUtils.isEmpty(one.getName())
                            && !StringUtils.isEmpty(one.getGroupName())
                    ).forEach(one -> {
                db.putIfAbsent(one.getGroupName(), new ArrayList<>());
                db.get(one.getGroupName()).add(new WorkerPoolStatusBuilder(one));
            });
        }
    }

    /**
     * Get status of all workerPool in same group
     * @param groupName group name, cf IWorkerPool.getGroupName
     * @return status of all workerPool in same group
     */
    @Override
    public WorkerPoolGroupStatus get(String groupName) {
        return new WorkerPoolGroupStatus(db.get(groupName));
    }
}
