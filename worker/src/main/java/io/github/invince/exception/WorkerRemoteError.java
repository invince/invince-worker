package io.github.invince.exception;

/**
 * Remote error, a serializable wrapper of the exception message.
 * For ex, if we are in distributed mode, when one node process a task and it fails, we need send failed event to other nodes, thus we need a serializable exception
 */
public class WorkerRemoteError extends WorkerException {

    public WorkerRemoteError(){
        super("remote worker fail to process task");
    }

    public WorkerRemoteError(String message) {
        super(message);
    }
}
