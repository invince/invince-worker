package com.invince.worker.adapter.redis.collections.model;

import com.invince.worker.core.BaseTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessingTaskWrapper<V extends BaseTask> implements Serializable {

    private V task;
    private String poolProcessIt;
}
