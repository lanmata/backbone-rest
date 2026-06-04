package com.umdc.backoffice.v1.session.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JtiDenyListServiceImplTest {

    private static final String JTI_PREFIX = "jti:";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private JtiDenyListServiceImpl jtiDenyListService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jtiDenyListService = new JtiDenyListServiceImpl(redisTemplate);
    }

    @Test
    void denyJti_storesKeyWithTtl() {
        // Arrange
        String jti = "test-jti-id";
        long ttlSeconds = 3600L;

        // Act
        jtiDenyListService.denyJti(jti, ttlSeconds);

        // Assert
        verify(valueOperations, times(1)).set(JTI_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    @Test
    void isDenied_returnsTrue_whenKeyExists() {
        // Arrange
        String jti = "denied-jti";
        when(redisTemplate.hasKey(JTI_PREFIX + jti)).thenReturn(Boolean.TRUE);

        // Act
        boolean result = jtiDenyListService.isDenied(jti);

        // Assert
        assertTrue(result);
    }

    @Test
    void isDenied_returnsFalse_whenKeyNotExists() {
        // Arrange
        String jti = "valid-jti";
        when(redisTemplate.hasKey(JTI_PREFIX + jti)).thenReturn(Boolean.FALSE);

        // Act
        boolean result = jtiDenyListService.isDenied(jti);

        // Assert
        assertFalse(result);
    }

    @Test
    void isDenied_returnsFalse_whenRedisReturnsNull() {
        // Arrange — Boolean.TRUE.equals(null) == false, so null is treated as "not denied"
        String jti = "unknown-jti";
        when(redisTemplate.hasKey(JTI_PREFIX + jti)).thenReturn(null);

        // Act
        boolean result = jtiDenyListService.isDenied(jti);

        // Assert
        assertFalse(result);
    }

    @Test
    void isDenied_propagatesException_whenRedisThrows() {
        // Arrange — implementation does not swallow Redis failures; exception propagates to caller
        String jti = "error-jti";
        when(redisTemplate.hasKey(JTI_PREFIX + jti)).thenThrow(new RuntimeException("Redis unavailable"));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> jtiDenyListService.isDenied(jti));
    }
}

