package io.github.invince.worker.demo;

import io.github.invince.util.WorkerPoolPredicate;
import io.github.invince.worker.core.AbstractTask;
import io.github.invince.worker.core.CompositeWorkerPool;
import io.github.invince.worker.core.StandardWorkerPool;
import io.github.invince.worker.core.WorkerPoolSetup;
import io.github.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeWorkerPoolTest {

    @Test
    void test() throws InterruptedException {

        var defaultPool_if_value_between_5_10 = new StandardWorkerPool2(new WorkerPoolSetup()
                .setQueueName("defaultPool_if_value_between_5_10").setMaxNbWorker(2));
        var ifValue_gt_10 = new StandardWorkerPool2(new WorkerPoolSetup()
                .setQueueName("ifValue_gt_10").setMaxNbWorker(1));
        var ifValue_lt_5 = new StandardWorkerPool2(new WorkerPoolSetup()
                .setQueueName("ifValue_lt_5").setMaxNbWorker(3));

        var compositePool = new CompositeWorkerPool<TaskWithValue>(
                defaultPool_if_value_between_5_10,
                List.of(
                        new WorkerPoolPredicate<>(ifValue_gt_10, task -> task.value > 10),
                        new WorkerPoolPredicate<>(ifValue_lt_5, task -> task.value < 5)
                )
        );

        var task1 = new TaskWithValue(1);
        var task2 = new TaskWithValue(2);
        var task3 = new TaskWithValue(3);
        var task4 = new TaskWithValue(4);
        var task5 = new TaskWithValue(5);
        var task6 = new TaskWithValue(6);
        var task7 = new TaskWithValue(7);
        var task8 = new TaskWithValue(8);
        var task9 = new TaskWithValue(9);
        var task10 = new TaskWithValue(10);
        var task11 = new TaskWithValue(11);
        var task12 = new TaskWithValue(12);

        compositePool.enqueue(task1);
        compositePool.enqueue(task2);
        compositePool.enqueue(task3);
        compositePool.enqueue(task4);
        compositePool.enqueue(task5);
        compositePool.enqueue(task6);
        compositePool.enqueue(task7);
        compositePool.enqueue(task8);
        compositePool.enqueue(task9);
        compositePool.enqueue(task10);
        compositePool.enqueue(task11);
        compositePool.enqueue(task12);

        Thread.sleep(3000); // we don't have the sync function on StandardWorkerPool, so we wait it

        assertTrue(task1.finished.get());
        assertEquals("ifValue_lt_5", task1.pool);
        assertTrue(task2.finished.get());
        assertEquals("ifValue_lt_5", task2.pool);
        assertTrue(task3.finished.get());
        assertEquals("ifValue_lt_5", task3.pool);
        assertTrue(task4.finished.get());
        assertEquals("ifValue_lt_5", task4.pool);
        assertTrue(task5.finished.get());
        assertEquals("defaultPool_if_value_between_5_10", task5.pool);
        assertTrue(task6.finished.get());
        assertEquals("defaultPool_if_value_between_5_10", task6.pool);
        assertTrue(task7.finished.get());
        assertEquals("defaultPool_if_value_between_5_10", task7.pool);
        assertTrue(task8.finished.get());
        assertEquals("defaultPool_if_value_between_5_10", task8.pool);
        assertTrue(task9.finished.get());
        assertEquals("defaultPool_if_value_between_5_10", task9.pool);
        assertTrue(task10.finished.get());
        assertEquals("defaultPool_if_value_between_5_10", task10.pool);
        assertTrue(task11.finished.get());
        assertEquals("ifValue_gt_10", task11.pool);
        assertTrue(task12.finished.get());
        assertEquals("ifValue_gt_10", task12.pool);

    }

    private static class StandardWorkerPool2 extends StandardWorkerPool<TaskWithValue> {

        public StandardWorkerPool2(WorkerPoolSetup config) {
            super(config);
        }

        @Override
        public TaskWithValue enqueue(TaskWithValue task) {
            task.pool = this.getConfig().getQueueName();
            return super.enqueue(task);
        }
    }

    private static class TaskWithValue extends AbstractTask {

        private final AtomicBoolean finished = new AtomicBoolean(false);

        private String pool;

        private int value;

        public TaskWithValue(int value) {
            this.value = value;
        }

        @Override
        protected void doProcess(CompletableTaskFuture<Void> taskFuture) {
            finished.set(true);
        }
    }
}
