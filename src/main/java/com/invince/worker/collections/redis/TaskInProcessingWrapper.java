package com.invince.worker.collections.redis;

import com.invince.worker.BaseTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskInProcessingWrapper<V extends BaseTask> implements Serializable {

    private V task;
    private String poolProcessIt;
}
