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

        SyncWithResultWorkerPool<NothingTask, String ,Integer, Integer> pool =
                new SyncWithResultWorkerPool<>(new WorkerPoolSetup().setMaxNbWorker(3), sumFunction);

        pool.enqueueAll("abc", List.of(new NothingTask(1), new NothingTask(2), new NothingTask(3)));
        pool.enqueueAll("def", List.of(new NothingTask(8), new NothingTask(2), new NothingTask(3)));

        Assertions.assertEquals(6, pool.waitResultUntilFinish("abc"));
        Assertions.assertEquals(13, pool.waitResultUntilFinish("def"));

        pool.shutdown(false);
    }

    static class NothingTask extends AbstractStandardTaskWithResult<Integer> {
        int value;

        public NothingTask(int value) {
            this.value = value;
        }

        @Override
        protected Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
            return value;
        }
    }

}
