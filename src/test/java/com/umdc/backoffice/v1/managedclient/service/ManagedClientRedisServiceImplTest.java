package com.umdc.backoffice.v1.managedclient.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for {@link ManagedClientRedisServiceImpl} using mocked {@link StringRedisTemplate}.
@ExtendWith(MockitoExtension.class)
class ManagedClientRedisServiceImplTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_JTI      = "test-jti-value";
    private static final String HASH_VALUE    = "$2a$10$prevHashValue";
    private static final String KEY_TOKEN     = "mcam:token:";
    private static final String KEY_REVOKED   = "mcam:revoked:";
    private static final String KEY_GRACE     = "mcam:grace:";
    private static final String KEY_RATELIMIT = "mcam:ratelimit:";
    private static final long   TTL_SECS      = 3600L;
    private static final long   GRACE_TTL     = 300L;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private SetOperations<String, String> setOps;

    private ManagedClientRedisServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ManagedClientRedisServiceImpl(redisTemplate);
    }

    // ── storeToken ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("storeToken — sets mcam:token:{jti} key with TTL")
    void storeToken_setsKeyWithTtl() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.storeToken(TEST_JTI, clientId, List.of("read:data"), TTL_SECS);

        verify(valueOps, times(1)).set(
                eq(KEY_TOKEN + TEST_JTI),
                any(String.class),
                eq(Duration.ofSeconds(TTL_SECS)));
    }

    // ── revokeToken ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("revokeToken — sets tombstone key mcam:revoked:{jti} with TTL")
    void revokeToken_setsTombstoneKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.revokeToken(TEST_JTI, TTL_SECS);

        verify(valueOps, times(1)).set(
                eq(KEY_REVOKED + TEST_JTI),
                eq("1"),
                eq(Duration.ofSeconds(TTL_SECS)));
    }

    // ── isRevoked ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isRevoked — tombstone key present returns true")
    void isRevoked_exists_returnsTrue() {
        when(redisTemplate.hasKey(KEY_REVOKED + TEST_JTI)).thenReturn(Boolean.TRUE);

        assertTrue(service.isRevoked(TEST_JTI));
    }

    @Test
    @DisplayName("isRevoked — tombstone key absent returns false")
    void isRevoked_absent_returnsFalse() {
        when(redisTemplate.hasKey(KEY_REVOKED + TEST_JTI)).thenReturn(Boolean.FALSE);

        assertFalse(service.isRevoked(TEST_JTI));
    }

    // ── isTokenStored ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenStored — active key present returns true")
    void isActive_exists_returnsTrue() {
        when(redisTemplate.hasKey(KEY_TOKEN + TEST_JTI)).thenReturn(Boolean.TRUE);

        assertTrue(service.isTokenStored(TEST_JTI));
    }

    @Test
    @DisplayName("isTokenStored — active key absent returns false")
    void isActive_absent_returnsFalse() {
        when(redisTemplate.hasKey(KEY_TOKEN + TEST_JTI)).thenReturn(Boolean.FALSE);

        assertFalse(service.isTokenStored(TEST_JTI));
    }

    // ── storeGraceSecret ──────────────────────────────────────────────────────

    @Test
    @DisplayName("storeGraceSecret — sets mcam:grace:{clientId} key with TTL")
    void storeGraceSecret_setsKeyWithTtl() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.storeGraceSecret(clientId, HASH_VALUE, GRACE_TTL);

        verify(valueOps, times(1)).set(
                eq(KEY_GRACE + clientId),
                eq(HASH_VALUE),
                eq(Duration.ofSeconds(GRACE_TTL)));
    }

    // ── getGraceSecret ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getGraceSecret — key present returns Optional with hash value")
    void getGraceSecret_present_returnsOptionalWithValue() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY_GRACE + clientId)).thenReturn(HASH_VALUE);

        Optional<String> result = service.getGraceSecret(clientId);

        assertTrue(result.isPresent());
        assertEquals(HASH_VALUE, result.get());
    }

    @Test
    @DisplayName("getGraceSecret — key absent returns Optional.empty")
    void getGraceSecret_absent_returnsEmpty() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY_GRACE + clientId)).thenReturn(null);

        Optional<String> result = service.getGraceSecret(clientId);

        assertFalse(result.isPresent());
    }

    // ── removeGraceSecret ─────────────────────────────────────────────────────

    @Test
    @DisplayName("removeGraceSecret — deletes mcam:grace:{clientId} key")
    void removeGraceSecret_deletesKey() {
        UUID clientId = UUID.randomUUID();

        service.removeGraceSecret(clientId);

        verify(redisTemplate, times(1)).delete(KEY_GRACE + clientId);
    }

    // ── checkAndIncrementRateLimit ────────────────────────────────────────────

    @Test
    @DisplayName("checkAndIncrementRateLimit — first call sets 60-second expiry")
    void incrementRateLimit_firstCall_setsExpiry() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(KEY_RATELIMIT + clientId)).thenReturn(1L);

        boolean allowed = service.checkAndIncrementRateLimit(clientId, 100);

        assertTrue(allowed);
        verify(redisTemplate, times(1)).expire(eq(KEY_RATELIMIT + clientId), eq(Duration.ofSeconds(60L)));
    }

    @Test
    @DisplayName("checkAndIncrementRateLimit — count within limit returns true")
    void incrementRateLimit_withinLimit_returnsTrue() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(KEY_RATELIMIT + clientId)).thenReturn(50L);

        boolean allowed = service.checkAndIncrementRateLimit(clientId, 100);

        assertTrue(allowed);
    }

    // ── addClientTokenJti / getClientTokenJtis ────────────────────────────────

    @Test
    @DisplayName("getClientTokenJtis — members present returns populated set")
    void getClientTokenJtis_withMembers_returnsSet() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members("mcam:client-tokens:" + clientId)).thenReturn(Set.of("jti1", "jti2"));

        Set<String> result = service.getClientTokenJtis(clientId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getClientTokenJtis — null from Redis returns empty set")
    void getClientTokenJtis_nullFromRedis_returnsEmptySet() {
        UUID clientId = UUID.randomUUID();
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members("mcam:client-tokens:" + clientId)).thenReturn(null);

        Set<String> result = service.getClientTokenJtis(clientId);

        assertTrue(result.isEmpty());
    }
}
