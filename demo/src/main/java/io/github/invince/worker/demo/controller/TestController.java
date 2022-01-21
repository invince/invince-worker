package io.github.invince.worker.demo.controller;

import io.github.invince.worker.demo.workerPool.Plus1Task;
import io.github.invince.worker.demo.workerPool.RedisWorkerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private RedisWorkerPool workerPool;

    @GetMapping("/api/enqueue/{group}/{value}")
    @ResponseBody
    public String enqueue(@PathVariable String group, @PathVariable int value) {
        workerPool.enqueueAll(group, List.of(new Plus1Task(value)));
        return "OK";
    }

    @GetMapping("/api/fetch/{group}")
    @ResponseBody
    public int fetchResult(@PathVariable String group) {
        return workerPool.waitResultUntilFinish(group);
    }

}
