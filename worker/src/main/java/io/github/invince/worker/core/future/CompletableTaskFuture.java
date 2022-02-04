package io.github.invince.worker.core.future;

import io.github.invince.exception.WorkerError;
import io.github.invince.worker.core.ITaskIdentify;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFuture to control if task is finished between different thread or node + task identify (prefix + key)
 * @param <SingleResult> singleResult of single task
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompletableTaskFuture <SingleResult> extends CompletableFuture<SingleResult> implements ITaskIdentify {

    private String prefix;
    private String key;
    private boolean toRetry;

    public CompletableTaskFuture(ITaskIdentify context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        this.prefix = context.getPrefix();
        this.key = context.getKey();
    }

}
