package io.github.invince.worker.core;

import io.github.invince.worker.adapter.local.future.DefaultCompletableTaskFutureService;
import io.github.invince.worker.core.collections.IProcessingTasks;
import io.github.invince.worker.core.collections.IToDoTasks;
import io.github.invince.worker.adapter.local.collections.DefaultProcessingTasks;
import io.github.invince.worker.adapter.local.collections.DefaultToDoTasks;
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

        StandardWorker<MyTask> worker = new StandardWorker<>(new DefaultCompletableTaskFutureService(), toDo, processing);
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