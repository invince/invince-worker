package io.github.invince.worker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardWorkerPoolTest {

    @BeforeEach
    void setUp() {
        MyTask.restart();
    }

    @Test
    void test() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3));

        var workerController = pool.workerController;

        pool.enqueue(new MyTask());
        assertEquals(1, workerController.getPermanentWorkers().size());

        pool.enqueue(new MyTask());
        assertEquals(2, workerController.getPermanentWorkers().size());

        pool.enqueue(new MyTask());
        assertEquals(3, workerController.getPermanentWorkers().size());

        pool.enqueue(new MyTask());
        assertEquals(3, workerController.getPermanentWorkers().size());

        pool.shutdown(true);

        assertEquals(4, MyTask.called.get());
    }


    @Test
    void testNotLazyCreation() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3).setLazyCreation(false));
        var workerController = pool.workerController;
        assertEquals(3, workerController.getPermanentWorkers().size());
    }

    @Test
    void testNoWorker() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(0));

        pool.enqueue(new MyTask());
        var workerController = pool.workerController;
        assertEquals(0, workerController.getPermanentWorkers().size());
        assertEquals(0, workerController.getTempWorkers().size());
    }

    @Test
    void testUnlimited() {
        StandardWorkerPool<MyTask> pool = new StandardWorkerPool<>(new WorkerPoolSetup().setUnlimited(true));

        pool.enqueue(new MyTask());
        var workerController = pool.workerController;
        assertEquals(1, workerController.getTempWorkers().size());
        assertEquals(0, workerController.getPermanentWorkers().size());

        pool.enqueue(new MyTask());
        assertEquals(2, workerController.getTempWorkers().size());
        assertEquals(0, workerController.getPermanentWorkers().size());

        pool.enqueue(new MyTask());
        assertEquals(3, workerController.getTempWorkers().size());
        assertEquals(0, workerController.getPermanentWorkers().size());

        pool.enqueue(new MyTask());
        assertEquals(4, workerController.getTempWorkers().size());
        assertEquals(0, workerController.getPermanentWorkers().size());

        pool.shutdown(true);

        assertEquals(4, MyTask.called.get());
    }
}