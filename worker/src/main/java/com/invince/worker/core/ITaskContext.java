package com.invince.worker.core;

public interface ITaskContext {
    String getPrefix();

    String getKey();

    default String getUniqueKey() {
        return getPrefix() + "_" + getKey();
    }
}
