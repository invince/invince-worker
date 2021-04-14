package com.invince.worker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinishTask extends AbstractTask {

    @Override
    protected void doProcess() {
        log.debug("Finish task process");
    }
}
