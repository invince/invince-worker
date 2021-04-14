package com.invince.worker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinishTaskTest {

    @Test
    void test() {
        FinishTask task = new FinishTask();
        task.process();
        assertNotNull(task.getKey());
    }
}
