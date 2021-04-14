package com.invince.worker.exception;

public class WorkerError extends WorkerException{

    private static final WorkerExceptionHelperBuilder helper = new WorkerExceptionHelperBuilder(WorkerError::new);

    public WorkerError(String message, Throwable e) {
        super(message, e);
    }

    public WorkerError(String message) {
        super(message);
    }

    public static WorkerExceptionHelperBuilder.WorkerExceptionHelper verify(String message) {
        return helper.build(message);
    }
}
