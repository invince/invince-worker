package com.invince.worker;

abstract class AbstractTaskWithResult<R> extends BaseTask<R> {

    public abstract R doProcess();
}
