package com.invince.worker.core;

import com.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinishTaskTest {

    @Test
    void test() {
        FinishTask task = new FinishTask();
        task.process(new DefaultCompletableTaskFutureService().getOrWrap(task));
        assertNotNull(task.getKey());
    }
}
