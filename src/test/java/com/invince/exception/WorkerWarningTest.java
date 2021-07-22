package com.invince.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkerWarningTest {

    @Test
    void testConstructor() {
        String msg = "hello";

        assertEquals(msg, new WorkerWarning(msg).getMessage());
        assertEquals(msg, new WorkerWarning(msg, new IllegalArgumentException()).getMessage());
    }

    @Test
    void testVerify() {
        assertThrows(WorkerWarning.class, () -> WorkerWarning.verify("OK").nonNull(null));
        assertThrows(WorkerWarning.class, () -> WorkerWarning.verify("OK").nonNull("test", "hello", null));

        WorkerWarning.verify("OK").nonNull("test", "hello");

        assertThrows(WorkerWarning.class, () -> WorkerWarning.verify("OK").isFalse(true));
        WorkerWarning.verify("OK").isFalse(false);
    }
}
