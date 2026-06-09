/*
 *  @(#)ManagedClientRedisServiceImpl.java
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
package com.umdc.backoffice.v1.managedclient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/// Redis-backed implementation of {@link ManagedClientRedisService}.
@Service
public class ManagedClientRedisServiceImpl implements ManagedClientRedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientRedisServiceImpl.class);

    private static final String KEY_TOKEN = "mcam:token:";
    private static final String KEY_REVOKED = "mcam:revoked:";
    private static final String KEY_CLIENT_TOKENS = "mcam:client-tokens:";
    private static final String KEY_GRACE = "mcam:grace:";
    private static final String KEY_RATELIMIT = "mcam:ratelimit:";
    private static final long RATE_LIMIT_WINDOW_SECONDS = 60L;

    private final StringRedisTemplate redisTemplate;

    /// Constructs a new {@code ManagedClientRedisServiceImpl}.
    ///
    /// @param redisTemplate the Spring Data Redis string template
    public ManagedClientRedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /// {@inheritDoc}
    @Override
    public void storeToken(String jti, UUID clientId, List<String> scopes, long ttlSeconds) {
        String payload = clientId.toString() + ":" + String.join(",", scopes);
        redisTemplate.opsForValue().set(KEY_TOKEN + jti, payload, Duration.ofSeconds(ttlSeconds));
        LOGGER.debug("Stored M2M token jti='{}' for clientId='{}'", jti, clientId);
    }

    /// {@inheritDoc}
    @Override
    public boolean isTokenStored(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_TOKEN + jti));
    }

    /// {@inheritDoc}
    @Override
    public void revokeToken(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(KEY_REVOKED + jti, "1", Duration.ofSeconds(ttlSeconds));
        LOGGER.debug("Revoked M2M token jti='{}'", jti);
    }

    /// {@inheritDoc}
    @Override
    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_REVOKED + jti));
    }

    /// {@inheritDoc}
    @Override
    public void addClientTokenJti(String jti, UUID clientId) {
        redisTemplate.opsForSet().add(KEY_CLIENT_TOKENS + clientId, jti);
    }

    /// {@inheritDoc}
    @Override
    public Set<String> getClientTokenJtis(UUID clientId) {
        Set<String> members = redisTemplate.opsForSet().members(KEY_CLIENT_TOKENS + clientId);
        return members != null ? members : Set.of();
    }

    /// {@inheritDoc}
    @Override
    public boolean checkAndIncrementRateLimit(UUID clientId, int maxRpm) {
        String key = KEY_RATELIMIT + clientId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (Long.valueOf(1L).equals(count)) {
            redisTemplate.expire(key, Duration.ofSeconds(RATE_LIMIT_WINDOW_SECONDS));
        }
        boolean allowed = count != null && count <= maxRpm;
        if (!allowed) {
            LOGGER.warn("Rate limit exceeded for clientId='{}' count={} maxRpm={}", clientId, count, maxRpm);
        }
        return allowed;
    }

    /// {@inheritDoc}
    @Override
    public void setGracePeriod(UUID clientId, long ttlSeconds) {
        redisTemplate.opsForValue().set(KEY_GRACE + clientId, "1", Duration.ofSeconds(ttlSeconds));
        LOGGER.debug("Grace period set for clientId='{}' ttl={} s", clientId, ttlSeconds);
    }

    /// {@inheritDoc}
    @Override
    public boolean isInGracePeriod(UUID clientId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_GRACE + clientId));
    }

    /// {@inheritDoc}
    @Override
    public void storeGraceSecret(UUID clientId, String prevHash, long graceTtlSeconds) {
        redisTemplate.opsForValue().set(KEY_GRACE + clientId, prevHash, Duration.ofSeconds(graceTtlSeconds));
        LOGGER.debug("Grace secret stored for clientId='{}' ttl={} s", clientId, graceTtlSeconds);
    }

    /// {@inheritDoc}
    @Override
    public Optional<String> getGraceSecret(UUID clientId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_GRACE + clientId));
    }

    /// {@inheritDoc}
    @Override
    public void removeGraceSecret(UUID clientId) {
        redisTemplate.delete(KEY_GRACE + clientId);
        LOGGER.debug("Grace secret removed for clientId='{}'", clientId);
    }
}
