/*
 *  @(#)JtiDenyListServiceImpl.java
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
package com.umdc.backoffice.v1.session.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed implementation of {@link JtiDenyListService}.
 * Stores denied JTI values under the key {@code jti:<value>} with the
 * remaining token TTL so entries are automatically evicted on expiry.
 */
@Service
public class JtiDenyListServiceImpl implements JtiDenyListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JtiDenyListServiceImpl.class);
    private static final String JTI_PREFIX = "jti:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Constructs a new JtiDenyListServiceImpl with the provided Redis template.
     *
     * @param redisTemplate the Spring Data Redis template for string operations
     */
    public JtiDenyListServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void denyJti(String jti, long ttlSeconds) {
        String key = JTI_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
        LOGGER.debug("JTI denied: {} with TTL {} seconds", jti, ttlSeconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDenied(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(JTI_PREFIX + jti));
    }
}

