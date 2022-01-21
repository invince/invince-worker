package io.github.invince.worker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyncWorkerPoolTest {

    @BeforeEach
    void setUp() {
        MyTask.restart();
    }

    @Test
    void test() {
        SyncWorkerPool<MyTask, Integer> pool = new SyncWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3));

        pool.enqueueAll(123, List.of(new MyTask(), new MyTask(), new MyTask()));
        pool.enqueueAll(456, List.of(new MyTask(), new MyTask(), new MyTask()));

        pool.waitUntilFinish(123);
        pool.waitUntilFinish(456);

        assertEquals(6, MyTask.called.get());
        pool.shutdown(false);
    }
}
