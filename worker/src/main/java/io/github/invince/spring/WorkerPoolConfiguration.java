package io.github.invince.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Spring configuration if you want use worker pool
 */
@Configuration
@ComponentScan({
        "io.github.invince.worker"
})
public class WorkerPoolConfiguration {

    /**
     * The profile you need active when you want use redis mode
     */
    public static final String PROFILE_REDIS = "redis-workerpool";

    /**
     * to bind the spring context to the SpringContextHolder
     * @param refreshedEvent when context refreshed
     */
    @EventListener({ContextRefreshedEvent.class})
    public void on(ContextRefreshedEvent refreshedEvent) {
        SpringContextHolder.init(refreshedEvent.getApplicationContext());
    }
}
