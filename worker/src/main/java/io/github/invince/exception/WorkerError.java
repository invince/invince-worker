package io.github.invince.exception;

/**
 * Error, worker should stop immediately
 */
public class WorkerError extends WorkerException {

    /**
     * Helper to check error
     */
    private static final WorkerExceptionHelperBuilder helper = new WorkerExceptionHelperBuilder(WorkerError::new);

    public WorkerError(String message, Throwable e) {
        super(message, e);
    }

    public WorkerError(String message) {
        super(message);
    }

    /**
     * @param message error message when verify fails
     * @return WorkerExceptionHelperBuilder
     */
    public static WorkerExceptionHelperBuilder.WorkerExceptionHelper verify(String message) {
        return helper.build(message);
    }
}
