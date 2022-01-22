package io.github.invince.exception;

import lombok.Getter;

/**
 * Task cancelled
 */
public class TaskCancelled extends RuntimeException {

    @Getter
    private final String key;

    /**
     *
     * @param key task key
     */
    public TaskCancelled(String key) {
        this.key = key;
    }
}
