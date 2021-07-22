package com.invince.exception;

public class WorkerRemoteError extends WorkerException{

    public WorkerRemoteError(){
        super("remote worker fail to process task");
    }

    public WorkerRemoteError(String message) {
        super(message);
    }
}
