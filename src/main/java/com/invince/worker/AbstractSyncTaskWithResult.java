package com.invince.worker;

public abstract class AbstractSyncTaskWithResult<R> extends AbstractTask  {

    public abstract R getResult();
}
