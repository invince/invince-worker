package io.github.invince.worker.core;

import java.io.Serializable;

/**
 * Task Identify
 */
public interface ITaskIdentify extends Serializable {
    String getPrefix();

    String getKey();

    default String getUniqueKey() {
        return getPrefix() + "_" + getKey();
    }
}
