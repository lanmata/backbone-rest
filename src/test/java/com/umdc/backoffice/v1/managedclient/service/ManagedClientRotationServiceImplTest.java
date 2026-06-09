package com.umdc.backoffice.v1.managedclient.service;

import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.jpa.domain.ManagedClientEntity;
import com.umdc.backoffice.jpa.repository.ManagedClientRepository;
import com.umdc.backoffice.property.ManagementAuthenticatorProperties;
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientSecretRotateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for {@link ManagedClientRotationServiceImpl} covering AC-ROT-01.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagedClientRotationServiceImplTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_NAME    = "rotation-client";
    private static final String CURR_HASH    = "$2a$10$currentSecretHash";
    private static final String NEW_HASH     = "$2a$10$newSecretHash";
    private static final String SCOPE_READ   = "read:data";
    private static final long   GRACE_PERIOD = 300L;
    private static final String ERR_NOT_FOUND = "not_found";

    @Mock
    private ManagedClientRepository repository;

    @Mock
    private ManagedClientSecretHashService secretHashService;

    @Mock
    private ManagedClientRedisService redisService;

    @Mock
    private ManagedClientAuditService auditService;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private ManagementAuthenticatorProperties mcamProps;

    private ManagedClientRotationServiceImpl service;

    @BeforeEach
    void setUp() {
        when(securityProperties.getManagementAuthenticator()).thenReturn(mcamProps);
        service = new ManagedClientRotationServiceImpl(
                repository, secretHashService, redisService, auditService, securityProperties);
    }

    // ── rotateSecret ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("rotateSecret — found active client returns 200 with new clientSecret (AC-ROT-01)")
    void rotateSecret_found_returns200WithNewSecret() throws Exception {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId);
        when(repository.findByIdAndActiveTrue(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.hashSecret(any())).thenReturn(CompletableFuture.completedFuture(NEW_HASH));
        when(mcamProps.getRotationGracePeriodSeconds()).thenReturn(GRACE_PERIOD);

        ResponseEntity<?> response = service.rotateSecret(clientId, "127.0.0.1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientSecretRotateResponse body = (ManagedClientSecretRotateResponse) response.getBody();
        assertNotNull(body);
        assertNotNull(body.getClientSecret());
        assertEquals(GRACE_PERIOD, body.getGracePeriodSeconds());
        assertNotNull(body.getRotatedAt());
    }

    @Test
    @DisplayName("rotateSecret — stores old hash in Redis with grace TTL (AC-ROT-01 grace)")
    void rotateSecret_found_storesGraceSecretInRedis() throws Exception {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId);
        when(repository.findByIdAndActiveTrue(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.hashSecret(any())).thenReturn(CompletableFuture.completedFuture(NEW_HASH));
        when(mcamProps.getRotationGracePeriodSeconds()).thenReturn(GRACE_PERIOD);

        service.rotateSecret(clientId, "127.0.0.1");

        verify(redisService, times(1)).storeGraceSecret(eq(clientId), eq(CURR_HASH), eq(GRACE_PERIOD));
    }

    @Test
    @DisplayName("rotateSecret — updates entity secretHash and prevSecretHash and saves")
    void rotateSecret_found_updatesEntitySecretHash() throws Exception {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId);
        when(repository.findByIdAndActiveTrue(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.hashSecret(any())).thenReturn(CompletableFuture.completedFuture(NEW_HASH));
        when(mcamProps.getRotationGracePeriodSeconds()).thenReturn(GRACE_PERIOD);

        service.rotateSecret(clientId, "127.0.0.1");

        ArgumentCaptor<ManagedClientEntity> captor = ArgumentCaptor.forClass(ManagedClientEntity.class);
        verify(repository, times(1)).save(captor.capture());
        ManagedClientEntity saved = captor.getValue();
        assertEquals(NEW_HASH, saved.getSecretHash());
        assertEquals(CURR_HASH, saved.getPrevSecretHash());
        assertNotNull(saved.getSecretLastRotatedAt());
    }

    @Test
    @DisplayName("rotateSecret — emits CLIENT_SECRET_ROTATED audit event")
    void rotateSecret_found_emitsAuditEvent() throws Exception {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId);
        when(repository.findByIdAndActiveTrue(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.hashSecret(any())).thenReturn(CompletableFuture.completedFuture(NEW_HASH));
        when(mcamProps.getRotationGracePeriodSeconds()).thenReturn(GRACE_PERIOD);

        service.rotateSecret(clientId, "127.0.0.1");

        verify(auditService, times(1)).record(
                eq(clientId), eq(AuditEventType.CLIENT_SECRET_ROTATED), any(), eq("SUCCESS"), any());
    }

    @Test
    @DisplayName("rotateSecret — not found or inactive returns 404")
    void rotateSecret_notFound_returns404() {
        UUID clientId = UUID.randomUUID();
        when(repository.findByIdAndActiveTrue(clientId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.rotateSecret(clientId, "127.0.0.1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ManagedClientErrorResponse body = (ManagedClientErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals(ERR_NOT_FOUND, body.getError());
    }

    @Test
    @DisplayName("rotateSecret — new clientSecret differs from stored hash (plaintext vs hash)")
    void rotateSecret_newSecretDiffersFromOld() throws Exception {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId);
        when(repository.findByIdAndActiveTrue(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.hashSecret(any())).thenReturn(CompletableFuture.completedFuture(NEW_HASH));
        when(mcamProps.getRotationGracePeriodSeconds()).thenReturn(GRACE_PERIOD);

        ResponseEntity<?> response = service.rotateSecret(clientId, "127.0.0.1");

        ManagedClientSecretRotateResponse body = (ManagedClientSecretRotateResponse) response.getBody();
        assertNotNull(body);
        assertNotEquals(CURR_HASH, body.getClientSecret());
        assertTrue(body.getGracePeriodSeconds() > 0);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientEntity buildEntity(UUID id) {
        ManagedClientEntity entity = new ManagedClientEntity();
        entity.setId(id);
        entity.setName(TEST_NAME);
        entity.setApplicationId(UUID.randomUUID());
        entity.setScopes(List.of(SCOPE_READ));
        entity.setActive(true);
        entity.setSecretHash(CURR_HASH);
        return entity;
    }
}
