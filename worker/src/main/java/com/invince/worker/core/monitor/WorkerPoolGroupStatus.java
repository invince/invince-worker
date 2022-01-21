package com.invince.worker.core.monitor;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * list of worker pool status in same group
 */
@Data
public class WorkerPoolGroupStatus {

    private List<WorkerPoolStatus> stats = new ArrayList<>();

    public WorkerPoolGroupStatus(List<WorkerPoolStatusBuilder> builders) {
        if (builders != null) {
            builders.stream().filter(Objects::nonNull)
                    .forEach(one -> this.stats.add(one.build()));
        }
    }
}
