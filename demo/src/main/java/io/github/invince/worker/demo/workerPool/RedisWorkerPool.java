package io.github.invince.worker.demo.workerPool;

import io.github.invince.worker.core.SyncWithResultWorkerPool;
import io.github.invince.worker.core.WorkerPoolSetup;
import io.github.invince.worker.core.helper.IWorkerPoolHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class RedisWorkerPool extends SyncWithResultWorkerPool<Plus1Task, String, Integer, Integer> {

    private final static Function<List<Integer>, Integer> sumFunction = listNumber -> listNumber.stream().reduce(0, Integer::sum);

    @Autowired
    public RedisWorkerPool(
            @Value("${worker.nbWorker}") int nbWorker,
            IWorkerPoolHelper redisHelper
            ) {
        super(new WorkerPoolSetup()
                .setMaxNbWorker(nbWorker)
                .setHelper(redisHelper)
                .setQueueName("shared") // NOTE: so 2 node share same queue
                .setLazyCreation(false), // NOTE: in redis mode it's import to set this to false
                sumFunction);
    }
}
