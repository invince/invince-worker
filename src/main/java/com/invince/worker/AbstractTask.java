package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class AbstractTask {

    private String defaultKey;

    private boolean done = false;
    private boolean error = false;

    public AbstractTask() {
        this.defaultKey = UUID.randomUUID().toString();
    }

    public void process() {
        try{
            doProcess();
            done = true;
        } catch (Exception e){
            log.error(e.getMessage(), e);
            error = true;
        }
    }

    protected abstract void doProcess();

    // you can override this
    public String getKey() {
        return defaultKey;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isError() {
        return error;
    }
}
