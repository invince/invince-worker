package com.invince.worker.collections;

import com.invince.worker.exception.WorkerError;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SubscribableBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private Consumer<E> addCallback = a -> {};
    private Consumer<E> takeCallback = a -> {};

    public SubscribableBlockingQueue<E> onAdd(Consumer<E> callback){
        if(callback != null) {
            this.addCallback = this.addCallback.andThen(callback);
        }
        return this;
    }

    public SubscribableBlockingQueue<E> onTake(Consumer<E> callback){
        if(callback != null) {
            this.takeCallback = this.takeCallback.andThen(callback);
        }
        return this;
    }

    @Override
    public boolean add(E e) {
        boolean rt = super.add(e);
        WorkerError.verify("Fail to call add callback").successfullyConsume(addCallback, e);
        return rt;
    }

    @Override
    public E take() throws InterruptedException {
        E rt = super.take();
        WorkerError.verify("Fail to call take callback").successfullyConsume(takeCallback, rt);
        return rt;
    }
}
