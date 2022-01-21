package io.github.invince.worker.core;

import io.github.invince.worker.core.future.CompletableTaskFuture;

import java.util.concurrent.atomic.AtomicInteger;

class MyTask extends AbstractTask {

    static AtomicInteger called = new AtomicInteger(0);

    @Override
    protected void doProcess(CompletableTaskFuture<Void> taskFuture) {
        called.incrementAndGet();
    }

    public static void restart() {
        called.set(0);
    }

}
