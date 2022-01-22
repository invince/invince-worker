package io.github.invince.exception;

/**
 * Task cancelled when it's in processing
 */
public class InProcessingTaskCancelled extends TaskCancelled {
    public InProcessingTaskCancelled(String key) {
        super(key);
    }
}
