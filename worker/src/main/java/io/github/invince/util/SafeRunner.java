package io.github.invince.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility to run code in safe mode, all exception will be catched
 */
@Slf4j
@UtilityClass
public class SafeRunner {

    /**
     * Run something and you don't need care the exception
     * @param runnable to run
     */
    public void run(Runnable runnable) {
        if (runnable != null) {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
