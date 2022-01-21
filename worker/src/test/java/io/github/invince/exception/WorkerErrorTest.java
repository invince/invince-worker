package io.github.invince.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkerErrorTest {

    @Test
    void testConstructor() {
        String msg = "hello";

        assertEquals(msg, new WorkerError(msg).getMessage());
        assertEquals(msg, new WorkerError(msg, new IllegalArgumentException()).getMessage());
    }

    @Test
    void testVerify() {
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").nonNull(null));
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").nonNull("test", "hello", null));

        WorkerError.verify("OK").nonNull("test", "hello");

        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").isFalse(true));
        WorkerError.verify("OK").isFalse(false);
    }
}
