# WorkerPool (blocking queue style)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Description
This tool can help you create a blocking queue style workerPool.
- The workerPool manage a list of worker to take task and process it from the blocking queue (toDo list)
- you can enqueue task into the pool and worker will process
Why do this?  
- in that way, you can control the nb of parallel task (for ex, if you call external service, but that service has limitation)
- you can even distribute your task in a shared queue (we provide redis version, but you can implement your own), so the replica/other app can join and process together

## How to use it
- in your pom add 
```
<dependency>
  <groupId>io.github.invince</groupId>
  <artifactId>workerpool</artifactId>
  <version>1.0.0</version>
</dependency>
```
- this project is spring based, include **WorkerPoolConfiguration** configuration.
```java
@Import(WorkerPoolConfiguration.class)
```
- first decide which kind of workerPool (cf [below](#different-type-of-workerpool))
- Then create the Task class based the parent task type of that workerPool type. for ex: for SyncWithResultWorkerPool, you need create a Task extends AbstractStandardTaskWithResult
  * NOTE: if you want to use distributed mode, your task class should be serializable, for ex: if you use spring, you cannot inject spring bean/service in it
  * in that case, to help you, we created a **SpringContextHolder** class, you can do SpringContextHolder.getInstance(xxxx)
  * if you have many not serializable things to inject, you'd better create a facade helper class and SpringContextHolder.getInstance(that helper class)
- create a new workerPool
  * either simply do new StandardWorkerPool<YourTaskClass>() (or other standard workerPool type)
  * or you can extend and develop your own workerPool class, for ex: a custom way to enqueue task. Or like enqueue a list of param, and split them into sub tasks
- now you can enqueue cancel task, and waitUntilFinish if it's SyncWorkerPool or waitResultUntilFinish for a SyncWithResultWorkerPool ...
- to active distributed **REDIS** mode cf [here](#redis-mode)
 
## Different type of workerPool

### StandardWorkerPool
- the basic workPool, enqueue task into the pool and worker process it
- you can also cancel a task

### SyncWorkerPool
- in additional of above, you can group your task when you enqueue them, and **waitUntilFinish** for all tasks in that group
- you can also cancel all the tasks in the groups

### SyncWithResultWorkerPool
- in additional of above, you can get the result of all the task in the group **waitResultUntilFinish** cf example: SyncWithResultWorkerPoolExample
  * NOTE: you need return a SingleResult for a single task
  * and provide a function to merge list of SingleResult into the GatheredResult, that will be the result of your task group

### ChainedSyncWithResultWorkerPool
- with this, you can chain workerPool together
- NOTE: since for the chained workerPool, task will be enqueued by previous workerPool. So you need define SingleResultWithParam class to pass both result from previous pool and parameter to generate task
- in each ChainedSyncWithResultWorkerPool, you need define a function to tell workerPool how to generate a task from SingleResultWithParam
- the current pool takes SingleResultWithParam (NOTE: not the task, each pool will generate task from it), process it and pass that to next chained pool
Example of usage: 
  - you can chain a loadPool, a enrichPool and a persistencePool
  - enqueue a list of SingleResultWithParam into it (the 1st loadPool)
  - loadPool will take a param, process it and pass it to enrich pool, then the persistencePool
  - each SingleResultWithParam is processed separately, so 1st param won't impact or block 2nd one
  - in that way, you'll never hold the whole result in your memory (if you free it correctly in the last pool), unless you want fetch the result via **waitChainedResultUntilFinish**
  
### CompositeWorkerPool
- with this, you can combine a list of workerPool (like a gateway)
- when you enqueue a task, we'll check workerPoolPredicate one by one, if predicate matches, the task will be redirected to that pool
- Example of usage:
  * you can define a workerPool for small task, and a workerPool for heavy task
  * if task is small, we enqueue it into small queue (for ex: if you're in redis mode, the working node for small queue can have 10 workers)
  * if it's heavy one, it goes to heavy queue (for ex: if you're in redis mode, the working node for heavy queue has only 1 worker)

## Different Mode

### Default Mode
- all in local

### Redis Mode
- we provide redis mode (using [redisson](https://github.com/redisson/redisson)) to share the todo blocking queue
- active **redis-workerpool** spring profile
- NOTE: if you launch your app with replica, these replicas will connect together
- if you want more advanced setup, you can setup for ex a front node to enqueue task, and other working node to handle them
   * NOTE: it can be achieved even if the front and worker are the same app, for front node, just set the nbWorker to 0, and for working node disable the **lazyCreation**
- Sync and Sync with result, cancel function are also implemented
- no load balancing implemented, the task will be taken to the nearest (to the one have min ping time to redis ) working node. But load balancing is possible:
   * we use redisson free version we don't have that function, if you use pro version you will have [BlockingFairQueue](https://github.com/redisson/redisson/wiki/7.-distributed-collections#713-blocking-fair-queue)
   * you can launch multiple working node with only one worker for each, then the working node can take only one task :), that will you load balance the tasks
- no monitoring mode for all connected node :(

### To develop your own mode
- please refer com.invince.worker.adapter package
- you need implement toDo list, processing list, taskGroup, and taskFuture

```java
@Service
public class RedisWorkerPoolExample {
    @Autowired
    public RedisWorkerPoolExample (
            @Value(xxxx) int nbWorker, // for front node set 0, for working node set nb worker you want per working node
            IWorkerPoolHelper redisHelper // if you import the WorkerPoolConfiguration and active the redis-workerpool
    ) {
        super(new WorkerPoolSetup()
                .setMaxNbWorker(nbWorker)
                .setLazyCreation(false)
                .setHelper(redisHelper));
        
    }
    
}

```

## Monitoring
- basic monitoring is created for local mode, you can check toDo, processing list size and nb of worker launched
