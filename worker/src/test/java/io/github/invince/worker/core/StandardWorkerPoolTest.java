package io.github.invince.worker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StandardWorkerPoolTest {

    @BeforeEach
    void setUp() {
        MyTask.restart();
    }

    @Test
    void test() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3));

        pool.enqueue(new MyTask());
        List workers = (List) ReflectionTestUtils.getField(pool, "permanentWorkers");
        assertEquals(1, workers.size());

        pool.enqueue(new MyTask());
        assertEquals(2, workers.size());

        pool.enqueue(new MyTask());
        assertEquals(3, workers.size());

        pool.enqueue(new MyTask());
        assertEquals(3, workers.size());

        pool.shutdown(false);

        assertEquals(4, MyTask.called.get());
    }


    @Test
    void testNotLazyCreation() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3).setLazyCreation(false));
        List workers = (List) ReflectionTestUtils.getField(pool, "permanentWorkers");

        assertEquals(3, workers.size());
    }

    @Test
    void testNoWorker() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(0));

        pool.enqueue(new MyTask());
        List workers = (List) ReflectionTestUtils.getField(pool, "permanentWorkers");

        assertEquals(0, workers.size());

        List tempWorkers = (List) ReflectionTestUtils.getField(pool, "tempWorkers");

        assertEquals(0, tempWorkers.size());
    }

    @Test
    void testUnlimited() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setUnlimited(true));

        pool.enqueue(new MyTask());
        List workers = (List) ReflectionTestUtils.getField(pool, "tempWorkers");
        List permanentWorkers = (List) ReflectionTestUtils.getField(pool, "permanentWorkers");
        assertEquals(1, workers.size());
        assertEquals(0, permanentWorkers.size());

        pool.enqueue(new MyTask());
        assertEquals(2, workers.size());
        assertEquals(0, permanentWorkers.size());

        pool.enqueue(new MyTask());
        assertEquals(3, workers.size());
        assertEquals(0, permanentWorkers.size());

        pool.enqueue(new MyTask());
        assertEquals(4, workers.size());
        assertEquals(0, permanentWorkers.size());

        pool.shutdown(false);

        assertEquals(4, MyTask.called.get());
    }
}