package com.invince.worker.demo;

import com.invince.worker.core.AbstractTask;
import com.invince.worker.core.SyncWorkerPool;
import com.invince.worker.core.WorkerPoolSetup;
import com.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncWorkerPoolTest {

    @Test
    void test() {
        var pool = new SyncWorkerPool<SimpleTask, String>(new WorkerPoolSetup().setMaxNbWorker(2));
        var taska1 = new SimpleTask();
        var taska2 = new SimpleTask();
        var taskb1 = new SimpleTask();
        var taskb2 = new SimpleTask();

        pool.enqueueAll("a", List.of(taska1, taska2));
        pool.enqueueAll("b", List.of(taskb1));
        pool.enqueueAll("b", List.of(taskb2));

        pool.waitUntilFinish("a");
        assertTrue(taska1.finished.get());
        assertTrue(taska2.finished.get());

        pool.waitUntilFinish("b");
        assertTrue(taskb1.finished.get());
        assertTrue(taskb2.finished.get());
    }

    private static class SimpleTask extends AbstractTask {

        private final AtomicBoolean finished = new AtomicBoolean(false);

        @Override
        protected void doProcess(CompletableTaskFuture<Void> taskFuture) {
            finished.set(true);
        }
    }
}
