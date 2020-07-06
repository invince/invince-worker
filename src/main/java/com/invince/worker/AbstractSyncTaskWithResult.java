package com.invince.worker;

public abstract class AbstractSyncTaskWithResult<R> extends BaseTask<R>  {

    public abstract R getResult();

    @Override
    void doComplete() {
        this.complete(getResult());
    }
}
