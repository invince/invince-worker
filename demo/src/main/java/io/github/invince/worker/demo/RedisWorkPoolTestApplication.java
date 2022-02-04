package io.github.invince.worker.demo;

import io.github.invince.spring.WorkerPoolConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Launch a local redis, for ex: via docker
 * - be careful, check if redis contains data from previous run, you may be blocked because previous task will never send FinishEvent
 * Launch 2 instance of your app (intellij run config is included in .run)
 * - NOTE: set different port
 * - set nbWorker of 1st node to 0
 * Do api call (intellij scratch.http is included)
 * - call GET api/enqueue/test/1 to 1st node
 * - call GET api/enqueue/test/2 to 1st node
 * - call GET api/enqueue/test/3 to 1st node
 * - * call GET api/enqueue/test/4 to 2nd node, you can even enqueue to 2nd node, they share the same todo queue
 *
 * - check the log of 2nd node, your process should be processed on it
 * - call GET api/fetch/test to fetch the result
 *
 */

@Import(WorkerPoolConfiguration.class)
@SpringBootApplication
public class RedisWorkPoolTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisWorkPoolTestApplication.class, args);
    }

}
