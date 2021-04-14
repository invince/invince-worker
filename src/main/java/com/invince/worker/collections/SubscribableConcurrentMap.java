package com.invince.worker.collections;

import com.invince.worker.exception.WorkerError;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SubscribableConcurrentMap<K,V> extends ConcurrentHashMap<K,V> {

    private BiConsumer<K,V> putCallback = (k,v) -> {};
    private BiConsumer<K,V> removeCallback = (k,v) -> {};

    public SubscribableConcurrentMap<K,V> onPut(BiConsumer<K,V> callback){
        if(callback != null) {
            this.putCallback = this.putCallback.andThen(callback);
        }
        return this;
    }

    public SubscribableConcurrentMap<K,V> onRemove(BiConsumer<K,V> callback){
        if(callback != null) {
            this.removeCallback = this.removeCallback.andThen(callback);
        }
        return this;
    }

    @Override
    public V put(K k, V v) {
        V rt = super.put(k, v);
        WorkerError.verify("Fail to call put callback")
                .successfullyConsume(putCallback, k, v);
        return rt;
    }

    @Override
    public V remove(Object key){
        boolean containsKey = containsKey(key);
        V rt = super.remove(key);
        if(containsKey) {
            WorkerError.verify("Fail to call add callback").successfullyConsume(removeCallback, (K)key, rt);
        }
        return rt;
    }
}
