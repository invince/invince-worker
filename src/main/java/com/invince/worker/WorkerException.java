package com.invince.worker;

public class WorkerException extends RuntimeException {
    public WorkerException(String message, InterruptedException e) {
        super(message, e);
    }

    public WorkerException(String message) {
        super(message);
    }
}
