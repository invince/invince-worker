package com.invince.exception;

public class WorkerWarning extends WorkerException{

    private static final WorkerExceptionHelperBuilder helper = new WorkerExceptionHelperBuilder(WorkerWarning::new);

    public WorkerWarning(String message, Throwable e) {
        super(message, e);
    }

    public WorkerWarning(String message) {
        super(message);
    }

    public static WorkerExceptionHelperBuilder.WorkerExceptionHelper verify(String message) {
        return helper.build(message);
    }
}
