package io.github.invince.exception;

/**
 * Task cancelled when it's in todo list
 */
public class ToDoTaskCancelled extends TaskCancelled {
    public ToDoTaskCancelled(String key) {
        super(key);
    }
}
