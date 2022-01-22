package io.github.invince.exception;

/**
 * Warning, worker can continue
 */
public class WorkerWarning extends WorkerException{

    /**
     * Helper to check error
     */
    private static final WorkerExceptionHelperBuilder helper = new WorkerExceptionHelperBuilder(WorkerWarning::new);

    public WorkerWarning(String message, Throwable e) {
        super(message, e);
    }

    public WorkerWarning(String message) {
        super(message);
    }

    /**
     *
     * @param message error message when verify fails
     * @return WorkerExceptionHelperBuilder
     */
    public static WorkerExceptionHelperBuilder.WorkerExceptionHelper verify(String message) {
        return helper.build(message);
    }
}
