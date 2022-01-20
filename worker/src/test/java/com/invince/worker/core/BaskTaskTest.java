package com.invince.worker.core;

import com.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import com.invince.worker.core.future.CompletableTaskFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaskTaskTest {

    @Test
    void getKey() {
        assertNotEquals(new BaseTaskTest().getKey(), new BaseTaskTest().getKey());
    }

    @Test
    void normal() {

        BaseTask task = new BaseTaskTest();
        var taskFuture = new DefaultCompletableTaskFutureService().getOrWrap(task);

        assertFalse(taskFuture.isDone());
        task.process(taskFuture);
        assertTrue(taskFuture.isDone());
    }


    private static class BaseTaskTest extends BaseTask {

        @Override
        void processInternal(CompletableTaskFuture taskFuture) {
            taskFuture.complete(null);
        }
    }

}
