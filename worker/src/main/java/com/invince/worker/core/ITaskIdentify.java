package com.invince.worker.core;

/**
 * Task Identify
 */
public interface ITaskIdentify {
    String getPrefix();

    String getKey();

    default String getUniqueKey() {
        return getPrefix() + "_" + getKey();
    }
}
