package com.invince.worker;

import java.util.concurrent.atomic.AtomicInteger;

class MyTask extends AbstractTask{

    static AtomicInteger called = new AtomicInteger(0);

    @Override
    protected void doProcess() {
        called.incrementAndGet();
    }

    public static void restart() {
        called.set(0);
    }
}
