package com.invince.worker.core;

import com.invince.exception.TaskCancelled;
import com.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import com.invince.worker.core.future.CompletableTaskFuture;
import com.invince.worker.core.future.ICompletableTaskFutureService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class CancelTest {

    @BeforeEach
    void setUp() {
        MyTask.restart();
    }

    @Test
    void testCancelProcessing() throws InterruptedException {
        SyncWorkerPool<MyTaskToCancel, Integer> pool = new SyncWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3));
        pool.enqueueAll(12, Arrays.asList(new MyTaskToCancel(), new MyTaskToCancel()));

        Thread.sleep(2000);
        pool.cancelGroup(12);
        Thread.sleep(10000);

        pool.requestTaskMap.getOrCreate(12).forEach(taskFuture -> {
            boolean testOK = false;
            try {
                taskFuture.join();
                testOK = true;
            } catch (CompletionException | TaskCancelled e) {
                testOK = true;
            } catch (Exception e) {
                fail();
            }
            assertTrue(testOK);
        });

        pool.shutdown(false);

    }


    @Test
    void testCancelTodo() throws InterruptedException {
        SyncWorkerPool<MyTaskToCancel, Integer> pool = new SyncWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3));
        pool.enqueueAll(12, Arrays.asList(new MyTaskToCancel(), new MyTaskToCancel()));

        pool.cancelGroup(12);
        Thread.sleep(10000);

        pool.requestTaskMap.getOrCreate(12).forEach(taskFuture -> {
            boolean testOK = false;
            try {
                taskFuture.join();
                testOK = true;
            } catch (CompletionException | TaskCancelled e) {
                testOK = true;
            } catch (Exception e) {
                fail();
            }
            assertTrue(testOK);
        });

        pool.shutdown(false);

    }


    private static class MyTaskToCancel extends MyTask {
        AtomicBoolean cancelledInProgress = new AtomicBoolean(false);

        @Override
        protected void doProcess(CompletableTaskFuture<Void> taskFuture) {
            try {
                checkPoint();
                Thread.sleep(3000);
                checkPoint();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }

        @Override
        protected void onCancelProcessing() {
            cancelledInProgress.set(true);
        }
    }
}
