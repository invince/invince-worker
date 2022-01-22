package io.github.invince.spring;

import io.github.invince.exception.WorkerError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * SpringContextHolder
 */
@Slf4j
public class SpringContextHolder {

    private static ApplicationContext context;

    private static final Semaphore murex = new Semaphore(1);

    static {
        try {
            murex.acquire();//we'll wait context e initialized then we can start use it
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkerError(e.getMessage(), e);
        }
    }

    private SpringContextHolder() {
    }

    /**
     * Bind the spring context
     * @param context spring context
     */
    public static synchronized void init(ApplicationContext context) {
        if (SpringContextHolder.context == null) {
            SpringContextHolder.context = context;
            murex.release();
        } else {
            log.warn("Context is already initialized");
        }
    }

    /**
     * Get instance of given class type
     * @param tClass class type
     * @param <T> class type
     * @return the bean if exists
     */
    public static <T> T getInstance(Class<T> tClass) {
        try {
            murex.acquire();
            if (context == null) {
                return null;
            }
            return context.getBean(tClass);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkerError(e.getMessage(), e);
        } finally {
            murex.release();
        }
    }

    /**
     * Get instance of given class type. By default returns the default value
     * @param tClass class type
     * @param <T> class type
     * @return the bean if exists, otherwise the default value
     */
    public static <T> T getInstanceOrDefault(Class<T> tClass, T defaultVal) {
        if (context == null) {
            return defaultVal;
        }
        try {
            return Optional.ofNullable(getInstance(tClass)).orElse(defaultVal);
        } catch (NoSuchBeanDefinitionException e) {
            return defaultVal;
        }
    }
}
