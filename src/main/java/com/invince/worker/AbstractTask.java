package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask extends BaseTask<Void> {
    @Override
    final void processInternal() {
        doProcess();
        getFuture().complete(null);
    }

    protected abstract void doProcess();
}
