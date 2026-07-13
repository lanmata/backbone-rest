/*
 *  @(#)LoginAttemptServiceImpl.java
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
package com.umdc.backoffice.security.bruteforce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis-backed implementation of {@link LoginAttemptService}.
 * <p>
 * Uses the key pattern {@code login:attempts:<alias>:<applicationId>} to store
 * the failure counter. A TTL of {@link LoginAttemptService#LOCK_TTL_MINUTES} minutes
 * is applied on the first failure so that the counter is evicted automatically.
 * </p>
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);
    private static final String KEY_PREFIX = "login:attempts:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Constructs a new {@code LoginAttemptServiceImpl}.
     *
     * @param redisTemplate the Spring Data Redis string template
     */
    public LoginAttemptServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordFailure(String alias, UUID applicationId) {
        String key = buildKey(alias, applicationId);
        Long attempts = redisTemplate.opsForValue().increment(key);
        // Set the TTL only when this is the first failure so subsequent increments
        // don't reset the expiry window.
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(LOCK_TTL_MINUTES));
        }
        if (attempts != null && attempts >= MAX_FAILURES) {
            LOGGER.warn("Account locked due to too many failed attempts — alias='{}', applicationId='{}'",
                    alias, applicationId);
        } else {
            LOGGER.debug("Failure recorded for alias='{}', applicationId='{}', attempts={}",
                    alias, applicationId, attempts);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordSuccess(String alias, UUID applicationId) {
        String key = buildKey(alias, applicationId);
        redisTemplate.delete(key);
        LOGGER.debug("Failure counter cleared for alias='{}', applicationId='{}'", alias, applicationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocked(String alias, UUID applicationId) {
        String key = buildKey(alias, applicationId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }
        try {
            return Long.parseLong(value) >= MAX_FAILURES;
        } catch (NumberFormatException e) {
            LOGGER.warn("Unexpected value '{}' in Redis key '{}'", value, key, e);
            return false;
        }
    }

    /**
     * Builds the Redis key for the given alias and applicationId.
     *
     * @param alias         the user alias
     * @param applicationId the application identifier; may be {@code null}
     * @return the Redis key string
     */
    private String buildKey(String alias, UUID applicationId) {
        return KEY_PREFIX + alias + ":" + applicationId;
    }
}

