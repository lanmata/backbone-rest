package com.umdc.backoffice.security.bruteforce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginAttemptServiceImplTest {

    private static final String KEY_PREFIX = "login:attempts:";
    private static final String ALIAS = "testUser";
    private static final String LOCKED_ALIAS = "lockedUser";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginAttemptServiceImpl loginAttemptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        loginAttemptService = new LoginAttemptServiceImpl(redisTemplate);
    }

    // ── recordFailure ──────────────────────────────────────────────────────────

    @Test
    void recordFailure_firstAttempt_setsTtl() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;
        when(valueOperations.increment(key)).thenReturn(1L);

        // Act
        loginAttemptService.recordFailure(ALIAS, appId);

        // Assert
        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(key, Duration.ofMinutes(LoginAttemptService.LOCK_TTL_MINUTES));
    }

    @Test
    void recordFailure_subsequentAttempt_doesNotResetTtl() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;
        when(valueOperations.increment(key)).thenReturn(3L);

        // Act
        loginAttemptService.recordFailure(ALIAS, appId);

        // Assert
        verify(valueOperations).increment(key);
        verify(redisTemplate, never()).expire(any(), any(Duration.class));
    }

    @Test
    void recordFailure_atMaxFailures_doesNotThrow() {
        // Arrange — reaching MAX_FAILURES should log a warning but not throw
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + LOCKED_ALIAS + ":" + appId;
        when(valueOperations.increment(key)).thenReturn((long) LoginAttemptService.MAX_FAILURES);

        // Act — must complete without exception
        loginAttemptService.recordFailure(LOCKED_ALIAS, appId);

        // Assert
        verify(valueOperations).increment(key);
    }

    @Test
    void recordFailure_nullIncrementResult_doesNotSetTtlOrThrow() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;
        when(valueOperations.increment(key)).thenReturn(null);

        // Act
        loginAttemptService.recordFailure(ALIAS, appId);

        // Assert — no expire call when Redis returns null
        verify(redisTemplate, never()).expire(any(), any(Duration.class));
    }

    // ── recordSuccess ──────────────────────────────────────────────────────────

    @Test
    void recordSuccess_deletesRedisKey() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;

        // Act
        loginAttemptService.recordSuccess(ALIAS, appId);

        // Assert
        verify(redisTemplate).delete(key);
    }

    @Test
    void recordSuccess_withNullApplicationId_deletesNullSuffixedKey() {
        // Arrange
        String key = KEY_PREFIX + ALIAS + ":null";

        // Act
        loginAttemptService.recordSuccess(ALIAS, null);

        // Assert
        verify(redisTemplate).delete(key);
    }

    // ── isLocked ───────────────────────────────────────────────────────────────

    @Test
    void isLocked_returnsFalse_whenKeyNotPresent() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;
        when(valueOperations.get(key)).thenReturn(null);

        // Act
        boolean result = loginAttemptService.isLocked(ALIAS, appId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isLocked_returnsTrue_whenCountEqualsMaxFailures() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + LOCKED_ALIAS + ":" + appId;
        when(valueOperations.get(key)).thenReturn(String.valueOf(LoginAttemptService.MAX_FAILURES));

        // Act
        boolean result = loginAttemptService.isLocked(LOCKED_ALIAS, appId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isLocked_returnsTrue_whenCountExceedsMaxFailures() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + LOCKED_ALIAS + ":" + appId;
        when(valueOperations.get(key)).thenReturn("10");

        // Act
        boolean result = loginAttemptService.isLocked(LOCKED_ALIAS, appId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isLocked_returnsFalse_whenCountBelowMaxFailures() {
        // Arrange
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;
        when(valueOperations.get(key)).thenReturn("3");

        // Act
        boolean result = loginAttemptService.isLocked(ALIAS, appId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isLocked_returnsFalse_whenValueIsNotNumeric() {
        // Arrange — corrupted Redis value should not throw; implementation returns false safely
        UUID appId = UUID.randomUUID();
        String key = KEY_PREFIX + ALIAS + ":" + appId;
        when(valueOperations.get(key)).thenReturn("corrupted-value");

        // Act
        boolean result = loginAttemptService.isLocked(ALIAS, appId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isLocked_withNullApplicationId_usesNullInKey() {
        // Arrange — applicationId=null produces key suffix ":null"
        String key = KEY_PREFIX + LOCKED_ALIAS + ":null";
        when(valueOperations.get(key)).thenReturn("5");

        // Act
        boolean result = loginAttemptService.isLocked(LOCKED_ALIAS, null);

        // Assert
        assertTrue(result);
    }
}

