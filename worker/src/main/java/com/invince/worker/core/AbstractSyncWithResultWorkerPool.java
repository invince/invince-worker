package com.invince.worker.core;

import com.invince.exception.TaskCancelled;
import com.invince.exception.WorkerWarning;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class AbstractSyncWithResultWorkerPool<T extends AbstractTaskWithResult<SingleResult>, GroupByType, SingleResult, GatheredResult>
        extends AbstractSyncWorkerPool<T, GroupByType, SingleResult> {

    private final Function<List<SingleResult>, GatheredResult> gatherFn;

    public AbstractSyncWithResultWorkerPool(WorkerPoolSetup config, Function<List<SingleResult>, GatheredResult> gatherFn) {
        super(config);
        this.gatherFn = gatherFn;
    }

    // NOTE: you can only wait one time, since we remove the group once you have the result
    GatheredResult waitResultUntilFinishInternal(GroupByType group) {
        GatheredResult rt = null;
        if (requestTaskMap.existNotEmptyGroup(group)) {
            rt = gatherFn.apply(
                    requestTaskMap.getOrCreate(group).stream()
                            .map(taskFuture -> {
                                SingleResult result = null;
                                try {
                                    result = taskFuture.join();
                                } catch (TaskCancelled e) {
                                    log.warn("Task {} cancelled, result will be null", e.getKey());
                                } catch (WorkerWarning e) {
                                    log.warn(e.getMessage(), e);
                                }
                                completableTaskFutureService.release(taskFuture);
                                return result;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
            requestTaskMap.remove(group);
        }
        return rt;
    }
}
