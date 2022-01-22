package io.github.invince.exception;

/**
 * All kind of exception during the process (excepts task being cancelled)
 */
public class WorkerException extends RuntimeException {
    public WorkerException(String message, Throwable e) {
        super(message, e);
    }

    public WorkerException(String message) {
        super(message);
    }
}
