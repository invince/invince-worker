package com.invince.worker.demo;

import com.invince.worker.core.AbstractStandardTaskWithResult;
import com.invince.worker.core.SyncWithResultWorkerPool;
import com.invince.worker.core.WorkerPoolSetup;
import com.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

class SyncWithResultWorkerPoolExample {

    @Test
    void test() {

        Function<List<Integer>, Integer> sumFunction = listNumber -> listNumber.stream().reduce(0, Integer::sum);

        SyncWithResultWorkerPool<Plus1Task, String ,Integer, Integer> pool =
                new SyncWithResultWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3), sumFunction);

        pool.enqueueAll("abc", List.of(new Plus1Task(1), new Plus1Task(2), new Plus1Task(3)));
        pool.enqueueAll("def", List.of(new Plus1Task(8), new Plus1Task(2), new Plus1Task(3)));

        Assertions.assertEquals(((1+1) + (2+1) + (3+1)), pool.waitResultUntilFinish("abc"));
        Assertions.assertEquals(((8+1) + (2+1) + (3+1)), pool.waitResultUntilFinish("def"));

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

}
