package com.invince.worker.core.future;

import com.invince.exception.WorkerError;
import com.invince.worker.core.ITaskIdentify;
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

    public CompletableTaskFuture(ITaskIdentify context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        this.prefix = context.getPrefix();
        this.key = context.getKey();
    }

}
