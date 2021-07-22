package com.invince.worker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaskTaskTest {

    @Test
    void getKey() {
        assertNotEquals(newTask().getKey(), newTask().getKey());
    }

    @Test
    void normal() {

        BaseTask task = newTask();

        assertFalse(task.getFuture().isDone());
        task.process();
        assertTrue(task.getFuture().isDone());
    }

    private BaseTask newTask(){
        return new BaseTask() {
            @Override
            void processInternal() {
                this.getFuture().complete(null);
            }
        };
    }
}
