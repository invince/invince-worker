package com.invince.worker.core.future;

import com.invince.exception.WorkerError;
import com.invince.worker.core.ITaskContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompletableTaskFuture <SingleResult> extends CompletableFuture<SingleResult> implements ITaskContext {

    private String prefix;
    private String key;

    public CompletableTaskFuture(ITaskContext context) {
        WorkerError.verify("Null context to copy")
                .nonNull(context)
                .notEmpty(context.getKey())
                .notEmpty(context.getPrefix());
        this.prefix = context.getPrefix();
        this.key = context.getKey();
    }

}
