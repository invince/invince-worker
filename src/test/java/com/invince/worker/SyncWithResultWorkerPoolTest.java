package com.invince.worker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyncWithResultWorkerPoolTest {

    @Test
    void test() {
        SyncWithResultWorkerPool<MyTaskWithResult, String ,Integer, Integer> pool =
                new SyncWithResultWorkerPool<>(3, list -> list.stream().reduce(0, Integer::sum));

        pool.enqueueAll("abc", List.of(new MyTaskWithResult(1), new MyTaskWithResult(2), new MyTaskWithResult(3)));
        pool.enqueueAll("def", List.of(new MyTaskWithResult(8), new MyTaskWithResult(2), new MyTaskWithResult(3)));

        assertEquals(6, pool.waitResultUntilFinish("abc"));
        assertEquals(13, pool.waitResultUntilFinish("def"));

        pool.shutdown(false);
    }

    static class MyTaskWithResult extends AbstractStandardTaskWithResult<Integer> {
        int value;

        public MyTaskWithResult(int value) {
            this.value = value;
        }

        @Override
        public Integer doProcess() {
            return value;
        }
    }

}
