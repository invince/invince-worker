package io.github.invince.exception;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkerExceptionHelperBuilderTest {

    @Test
    void testVerify() {
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").nonNull(null));
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").nonNull("test", "hello", null));

        WorkerError.verify("OK").nonNull("test", "hello");

        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").isFalse(true));
        WorkerError.verify("OK").isFalse(false);

        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").isTrue(false));
        WorkerError.verify("OK").isTrue(true);

        String test = null;
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").notEmpty(test));
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").notEmpty(""));
        WorkerError.verify("OK").notEmpty("hello");

        Map map = null;
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").notEmpty(map));
        WorkerError.verify("OK").notEmpty(Map.of("hello", "world"));

        List list = null;
        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").notEmpty(list));
        WorkerError.verify("OK").notEmpty(List.of("hello", "world"));

        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").successfullyRun(()-> {
            throw new IllegalArgumentException();
        }));
        WorkerError.verify("OK").successfullyRun(()-> {});

        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").successfullyConsume( i-> {
            throw new IllegalArgumentException();
        }, 1));
        WorkerError.verify("OK").successfullyConsume( i-> i ++, 1);

        assertThrows(WorkerError.class, () -> WorkerError.verify("OK").successfullyConsume( (i,l) -> {
            throw new IllegalArgumentException();
        }, 1, 2));
        WorkerError.verify("OK").successfullyConsume( (i,l)-> i ++, 1, 2);
    }
}
