package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class AbstractTask extends BaseTask<Void> {
    @Override
    void doComplete() {
        this.complete(null);
    }
}
