package com.invince.worker.demo;

import com.invince.worker.core.AbstractChainedTaskWithResult;
import com.invince.worker.core.ChainedSyncWithResultWorkerPool;
import com.invince.worker.core.WorkerPoolSetup;
import com.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

class ChainedSyncWithResultWorkerPoolExample {

    private static Function<List<Integer>, Integer> sumFn = val -> val.stream().reduce(0,Integer::sum);

    @Test
    void test() {

        Plus1WorkerPool chain = (Plus1WorkerPool) new Plus1WorkerPool()
                .chain(new Multiple2WorkerPool())
                .chain(new SquareWorkerPool());

        chain.enqueue("((2+1)*2)^2+((3+1)*2)^2", 2);
        chain.enqueue("((2+1)*2)^2+((3+1)*2)^2", 3);

        chain.enqueueAll2("((4+1)*2)^2+((11+1)*2)^2", List.of(4, 11));

        Assertions.assertEquals(100, chain.waitResultUntilFinish("((2+1)*2)^2+((3+1)*2)^2"));
        Assertions.assertEquals(676, chain.waitResultUntilFinish("((4+1)*2)^2+((11+1)*2)^2"));
    }

    private class Plus1Task extends AbstractChainedTaskWithResult<String, Integer> {

        @Override
        public Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
            return this.param + 1;
        }

    }

    private class Plus1WorkerPool extends ChainedSyncWithResultWorkerPool<Plus1Task, String, Integer, Integer> {

        public Plus1WorkerPool() {
            super(new WorkerPoolSetup().setMaxNbWorker(2));
        }

        @Override
        protected Plus1Task newTask(String group, Integer integer) {
            return (Plus1Task) new Plus1Task().init(group, integer);
        }
    }

    private static class Multiple2Task extends AbstractChainedTaskWithResult<String, Integer> {

        @Override
        public Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
            return this.param * 2;
        }
    }

    private static class Multiple2WorkerPool extends ChainedSyncWithResultWorkerPool<Multiple2Task, String, Integer, Integer> {

        public Multiple2WorkerPool() {
            super(new WorkerPoolSetup().setMaxNbWorker(2));
        }

        @Override
        protected Multiple2Task newTask(String group, Integer integer) {
            return (Multiple2Task) new Multiple2Task().init(group, integer);
        }
    }

    private static class SquareTask extends AbstractChainedTaskWithResult<String, Integer> {

        @Override
        public Integer doProcess(CompletableTaskFuture<Integer> taskFuture) {
            return this.param * this.param;
        }
    }

    private static class SquareWorkerPool extends ChainedSyncWithResultWorkerPool<SquareTask, String, Integer, Integer> {

        public SquareWorkerPool() {
            super(new WorkerPoolSetup().setMaxNbWorker(2), sumFn);
        }

        @Override
        protected SquareTask newTask(String group, Integer integer) {
            return (SquareTask) new SquareTask().init(group, integer);
        }
    }
}
