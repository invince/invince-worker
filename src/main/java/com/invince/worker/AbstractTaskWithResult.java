package com.invince.worker;

abstract class AbstractTaskWithResult<R> extends BaseTask<R> {

    protected abstract R doProcess();
}
