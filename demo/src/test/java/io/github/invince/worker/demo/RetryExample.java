package io.github.invince.worker.demo;

import io.github.invince.exception.WorkerException;
import io.github.invince.worker.core.AbstractStandardTaskWithResult;
import io.github.invince.worker.core.SyncWithResultWorkerPool;
import io.github.invince.worker.core.WorkerPoolSetup;
import io.github.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class RetryExample {

    private static AtomicInteger totalTries;

    @BeforeEach
    void setUp() {
        totalTries = new AtomicInteger(0);
    }

    @Test
    void enoughForRetry() {

        Function<List<Integer>, Integer> sumFunction = listNumber -> listNumber.stream().reduce(0, Integer::sum);

        SyncWithResultWorkerPool<Plus1Task, String ,Integer, Integer> pool =
                new SyncWithResultWorkerPool<>(new WorkerPoolSetup()
                        .setMaxNbWorker(3)
                        .setMaxRetryTimes(5), sumFunction);

        pool.enqueueAll("abc", List.of(new RetryTask(1), new RetryTask(2), new Plus1Task(3)));
        pool.enqueueAll("def", List.of(new Plus1Task(8), new RetryTask(2), new Plus1Task(3)));

        Assertions.assertEquals(((1+1) + (2+1) + (3+1)), pool.waitResultUntilFinish("abc"));
        Assertions.assertEquals(((8+1) + (2+1) + (3+1)), pool.waitResultUntilFinish("def"));

        Assertions.assertEquals(9, totalTries.get(), "We retry 3 times for 3 tasks, so total = 9");

        pool.shutdown(false);
    }


    @Test
    void notEnoughForRetry() {

        Function<List<Integer>, Integer> sumFunction = listNumber -> listNumber.stream().reduce(0, Integer::sum);

        SyncWithResultWorkerPool<Plus1Task, String ,Integer, Integer> pool =
                new SyncWithResultWorkerPool<>(new WorkerPoolSetup()
                        .setMaxNbWorker(3)
                        .setMaxRetryTimes(1), sumFunction); // first time + 1 retry = 2 times

        pool.enqueueAll("abc", List.of(new RetryTask(1)));

        Assertions.assertThrows(CompletionException.class, () -> pool.waitResultUntilFinish("abc"));

        Assertions.assertEquals(2, totalTries.get(), "We can only try max 2 times ");

        pool.shutdown(false);
    }


    static class Plus1Task extends AbstractStandardTaskWithResult<Integer> {
        int value;

        public Plus1Task(int value) {
            this.value = value;
        }

        @Override
        protected Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
            return value + 1;
        }
    }

    static class RetryTask extends Plus1Task {

        int counterBeforeSuccess = 3;

        public RetryTask(int value) {
            super(value);
        }

        @Override
        protected Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
            if(counterBeforeSuccess -- > 0) {
                totalTries.incrementAndGet();
                throw new WorkerException("Fake");
            }
            return value + 1;
        }
    }

}
