package com.invince.worker.collections;

import com.invince.worker.FinishTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class SubscribableBlockingQueueTest {

    private AtomicInteger aCounter;
    private AtomicInteger tCounter;

    private Consumer<FinishTask> a;
    private Consumer<FinishTask> t;

    @BeforeEach
    void setUp() {
        aCounter = new AtomicInteger(0);
        tCounter = new AtomicInteger(0);

        a = finishTask -> aCounter.incrementAndGet();
        t = finishTask -> tCounter.incrementAndGet();
    }

    @Test
    void basic() throws InterruptedException {
        var list = new SubscribableBlockingQueue<FinishTask>();
        list.onTake(null).onAdd(null);

        list.add(new FinishTask());
        list.take();

        // no exception
    }

    @Test
    void onAdd() throws InterruptedException {
        var list = new SubscribableBlockingQueue<FinishTask>();
        list.onAdd(a);
        list.add(new FinishTask());
        assertEquals(1, aCounter.get());
        list.add(new FinishTask());
        assertEquals(2, aCounter.get());
    }

    @Test
    void onTake() throws InterruptedException {
        var list = new SubscribableBlockingQueue<FinishTask>();
        list.onTake(t);
        list.add(new FinishTask());
        list.add(new FinishTask());
        list.take();
        assertEquals(1, tCounter.get());
        list.take();
        assertEquals(2, tCounter.get());
    }
}
