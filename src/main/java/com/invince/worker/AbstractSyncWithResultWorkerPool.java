package com.invince.worker;

import com.invince.exception.TaskCancelled;
import com.invince.exception.WorkerWarning;
import com.invince.spring.ContextHolder;
import com.invince.worker.future.ICompletableTaskService;
import com.invince.worker.future.local.DefaultCompletableTaskService;
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
            var completableTaskService = ContextHolder.getInstanceOrDefault(ICompletableTaskService.class, new DefaultCompletableTaskService());
            rt = gatherFn.apply(
                    requestTaskMap.getOrCreate(group).stream()
                            .map(task -> {
                                SingleResult result = null;
                                try {
                                    result = task.getFuture().join();
                                } catch (TaskCancelled e) {
                                    log.warn("Task {} cancelled, result will be null", e.getKey());
                                } catch (WorkerWarning e) {
                                    log.warn(e.getMessage(), e);
                                }
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
