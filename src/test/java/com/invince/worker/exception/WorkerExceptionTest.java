package com.invince.worker.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkerExceptionTest {

    @Test
    void testConstructor() {
        String msg = "hello";

        assertEquals(msg, new WorkerException(msg).getMessage());
        assertEquals(msg, new WorkerException(msg, new IllegalArgumentException()).getMessage());
    }
}
