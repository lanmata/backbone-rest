/*
 *  @(#)ManagedClientAsyncConfig.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.v1.managedclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/// Async and scheduling configuration for the MCAM feature.
/// <p>
/// Provides a dedicated thread pool for BCrypt hashing so that blocking
/// operations do not exhaust Tomcat I/O threads under load (NFR-P-03).
/// {@code @EnableScheduling} activates the maintenance cleanup task.
/// </p>
@Configuration
@EnableAsync
@EnableScheduling
public class ManagedClientAsyncConfig {

    /// Thread pool for BCrypt hashing operations.
    /// Prevents BCrypt from blocking Tomcat I/O threads under load (NFR-P-03).
    ///
    /// @return the configured {@link ThreadPoolTaskExecutor}
    @Bean("mcamHashExecutor")
    public ThreadPoolTaskExecutor mcamHashExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mcam-hash-");
        executor.initialize();
        return executor;
    }
}
