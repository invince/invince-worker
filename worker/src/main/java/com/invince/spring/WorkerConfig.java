package com.invince.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@ComponentScan({
        "com.invince.worker"
})
public class WorkerConfig {

    public static final String PROFILE_REDIS = "redis-workerpool";

    @EventListener({ContextRefreshedEvent.class})
    public void on(ContextRefreshedEvent refreshedEvent) {
        SpringContextHolder.init(refreshedEvent.getApplicationContext());
    }
}
