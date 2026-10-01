/*
 *  @(#)ManagedClientAsyncConfigTest.java
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class ManagedClientAsyncConfigTest {

    private ThreadPoolTaskExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("mcamHashExecutor is configured with the documented pool sizing and prefix")
    void mcamHashExecutor_configuredAsDocumented() {
        var config = new ManagedClientAsyncConfig();
        executor = config.mcamHashExecutor();

        Assertions.assertAll("Test executor configuration",
                () -> Assertions.assertEquals(2, executor.getCorePoolSize()),
                () -> Assertions.assertEquals(4, executor.getMaxPoolSize()),
                () -> Assertions.assertEquals("mcam-hash-", executor.getThreadNamePrefix())
        );
    }

    @Test
    @DisplayName("mcamHashExecutor is initialized and can actually execute a task")
    void mcamHashExecutor_executesSubmittedTask() throws Exception {
        var config = new ManagedClientAsyncConfig();
        executor = config.mcamHashExecutor();

        CompletableFuture<String> future = CompletableFuture.supplyAsync(
                () -> Thread.currentThread().getName(), executor);
        String threadName = future.get(5, TimeUnit.SECONDS);

        Assertions.assertTrue(threadName.startsWith("mcam-hash-"));
    }
}
