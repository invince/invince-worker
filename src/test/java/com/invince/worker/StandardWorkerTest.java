package com.invince.worker;

import com.invince.worker.collections.IProcessingTasks;
import com.invince.worker.collections.IToDoTasks;
import com.invince.worker.collections.local.DefaultProcessingTasks;
import com.invince.worker.collections.local.DefaultToDoTasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class StandardWorkerTest {

    @BeforeEach
    void setUp() {
        MyTask.restart();
    }

    @Test
    void test() throws InterruptedException {

        IToDoTasks toDo = new DefaultToDoTasks();
        IProcessingTasks<String, MyTask> processing = new DefaultProcessingTasks<>();

        StandardWorker<MyTask> worker = new StandardWorker<>(toDo, processing);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(worker);

        toDo.add(new MyTask());
        toDo.add(new MyTask());
        toDo.add(new FinishTask());

        executorService.awaitTermination(10, TimeUnit.SECONDS);
        Integer counter = (Integer) ReflectionTestUtils.getField(worker, "counter");
        assertEquals(2, counter);
    }
}