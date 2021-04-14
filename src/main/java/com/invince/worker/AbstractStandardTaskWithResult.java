package com.invince.worker;

public abstract class AbstractStandardTaskWithResult<R> extends AbstractTaskWithResult<R>{

    @Override
    final void processInternal() {
        this.complete(doProcess());
    }
}
