package com.karthik.incidentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Dedicated thread pool for AI triage so a slow or hung Groq API call can
 * never starve Tomcat's own request-handling threads.
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "aiTriageExecutor")
    public Executor aiTriageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ai-triage-");
        executor.initialize();
        return executor;
    }
}
