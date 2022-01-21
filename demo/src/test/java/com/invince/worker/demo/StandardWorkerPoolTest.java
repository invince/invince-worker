package com.invince.worker.demo;

import com.invince.worker.core.AbstractTask;
import com.invince.worker.core.StandardWorkerPool;
import com.invince.worker.core.WorkerPoolSetup;
import com.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardWorkerPoolTest {

    @Test
    void test() throws InterruptedException {
        var pool = new StandardWorkerPool<SimpleTask>(new WorkerPoolSetup().setMaxNbWorker(2));
        var task = new SimpleTask();
        var task2 = new SimpleTask();
        pool.enqueue(task);
        pool.enqueue(task2);


        Thread.sleep(3000); // we don't have the sync function on StandardWorkerPool, so we wait it

        assertTrue(task.finished.get());
        assertTrue(task2.finished.get());

    }

    private static class SimpleTask extends AbstractTask {

        private final AtomicBoolean finished = new AtomicBoolean(false);

        @Override
        protected void doProcess(CompletableTaskFuture<Void> taskFuture) {
            finished.set(true);
        }
    }
}
