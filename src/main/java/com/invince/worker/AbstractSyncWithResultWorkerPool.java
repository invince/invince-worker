package com.invince.worker;

import com.invince.spring.ContextHolder;
import com.invince.worker.future.ICompletableTaskService;
import com.invince.worker.future.local.DefaultCompletableTaskService;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            var completableTaskService = ContextHolder.getInstanceOrDefault(ICompletableTaskService.class, new DefaultCompletableTaskService());
            rt = gatherFn.apply(
                    requestTaskMap.getOrCreate(group).stream()
                            .map(task -> {
                                SingleResult result = task.getFuture().join();
                                completableTaskService.release(task);
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
