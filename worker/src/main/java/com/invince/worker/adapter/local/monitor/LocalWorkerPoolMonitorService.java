package com.invince.worker.adapter.local.monitor;

import com.invince.worker.core.StandardWorkerPool;
import com.invince.worker.core.monitor.IWorkerPoolMonitorService;
import com.invince.worker.core.monitor.WorkerPoolGroupStatus;
import com.invince.worker.core.monitor.WorkerPoolStatusBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalWorkerPoolMonitorService implements IWorkerPoolMonitorService {

    private final Map<String, List<WorkerPoolStatusBuilder>> db = new HashMap<>();

    @Autowired
    public LocalWorkerPoolMonitorService(List<StandardWorkerPool<?>> pools) {
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

    @Override
    public WorkerPoolGroupStatus get(String groupName) {
        return new WorkerPoolGroupStatus(db.get(groupName));
    }
}
