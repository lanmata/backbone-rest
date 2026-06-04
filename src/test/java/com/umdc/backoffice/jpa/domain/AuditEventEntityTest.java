package com.umdc.backoffice.jpa.domain;

import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.jpa.domain.AuditEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuditEventEntityTest {

    private static final String TEST_IP     = "192.168.1.1";
    private static final String TEST_AGENT  = "JUnit/5.0";
    private static final String TEST_JSON   = "{\"action\":\"login\"}";

    // ── @PrePersist lifecycle ──────────────────────────────────────────────────

    @Test
    @DisplayName("prePersist sets occurredAt and createdAt when both are null")
    void prePersistSetsTimestampsWhenNull() {
        AuditEventEntity entity = new AuditEventEntity();
        assertNull(entity.getOccurredAt());
        assertNull(entity.getCreatedAt());

        entity.prePersist();

        assertNotNull(entity.getOccurredAt());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("prePersist does NOT overwrite occurredAt when already set")
    void prePersistDoesNotOverwriteOccurredAt() {
        LocalDateTime expected = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        AuditEventEntity entity = new AuditEventEntity();
        entity.setOccurredAt(expected);

        entity.prePersist();

        assertEquals(expected, entity.getOccurredAt());
    }

    @Test
    @DisplayName("prePersist does NOT overwrite createdAt when already set")
    void prePersistDoesNotOverwriteCreatedAt() {
        LocalDateTime expected = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        AuditEventEntity entity = new AuditEventEntity();
        entity.setCreatedAt(expected);

        entity.prePersist();

        assertEquals(expected, entity.getCreatedAt());
    }

    // ── getters / setters ─────────────────────────────────────────────────────

    @Test
    @DisplayName("all getters and setters round-trip correctly")
    void allGettersAndSettersWorkCorrectly() {
        UUID id     = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID appId  = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setApplicationId(appId);
        entity.setEventType(AuditEventType.LOGIN_SUCCESS);
        entity.setIpAddress(TEST_IP);
        entity.setUserAgent(TEST_AGENT);
        entity.setDetails(TEST_JSON);
        entity.setOccurredAt(now);
        entity.setCreatedAt(now);

        assertEquals(id, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals(appId, entity.getApplicationId());
        assertEquals(AuditEventType.LOGIN_SUCCESS, entity.getEventType());
        assertEquals(TEST_IP, entity.getIpAddress());
        assertEquals(TEST_AGENT, entity.getUserAgent());
        assertEquals(TEST_JSON, entity.getDetails());
        assertEquals(now, entity.getOccurredAt());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    @DisplayName("full constructor sets all fields correctly")
    void fullConstructorSetsAllFields() {
        UUID id     = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID appId  = UUID.randomUUID();

        AuditEventEntity entity = new AuditEventEntity(
                id, userId, appId, AuditEventType.LOGOUT,
                TEST_IP, TEST_AGENT, TEST_JSON);

        assertEquals(id, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals(appId, entity.getApplicationId());
        assertEquals(AuditEventType.LOGOUT, entity.getEventType());
        assertEquals(TEST_IP, entity.getIpAddress());
        assertEquals(TEST_AGENT, entity.getUserAgent());
        assertEquals(TEST_JSON, entity.getDetails());
    }

    // ── AuditEventType enum ───────────────────────────────────────────────────

    @Test
    @DisplayName("AuditEventType enum has exactly 19 values (10 original + 9 MCAM M2M)")
    void auditEventTypeEnumHasTenValues() {
        assertEquals(19, AuditEventType.values().length);
    }

    @Test
    @DisplayName("AuditEventType enum contains all expected values accessible by name")
    void auditEventTypeEnumContainsAllExpectedValues() {
        assertEquals(AuditEventType.LOGIN_SUCCESS,        AuditEventType.valueOf("LOGIN_SUCCESS"));
        assertEquals(AuditEventType.LOGIN_FAILURE,        AuditEventType.valueOf("LOGIN_FAILURE"));
        assertEquals(AuditEventType.PASSWORD_CHANGE,      AuditEventType.valueOf("PASSWORD_CHANGE"));
        assertEquals(AuditEventType.ROLE_ASSIGNED,        AuditEventType.valueOf("ROLE_ASSIGNED"));
        assertEquals(AuditEventType.ROLE_REVOKED,         AuditEventType.valueOf("ROLE_REVOKED"));
        assertEquals(AuditEventType.LOGOUT,               AuditEventType.valueOf("LOGOUT"));
        assertEquals(AuditEventType.ACCOUNT_LOCKED,       AuditEventType.valueOf("ACCOUNT_LOCKED"));
        assertEquals(AuditEventType.ACCOUNT_UNLOCKED,     AuditEventType.valueOf("ACCOUNT_UNLOCKED"));
        assertEquals(AuditEventType.TOKEN_REFRESH,        AuditEventType.valueOf("TOKEN_REFRESH"));
        assertEquals(AuditEventType.PASSWORD_RESET_REQUEST, AuditEventType.valueOf("PASSWORD_RESET_REQUEST"));
    }
}

