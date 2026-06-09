package com.umdc.backoffice.v1.iam.audit.service;

import com.umdc.backoffice.v1.iam.audit.api.to.AuditEventTO;
import com.umdc.backoffice.jpa.domain.AuditEventEntity;
import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.v1.iam.audit.mapper.AuditEventMapper;
import com.umdc.backoffice.jpa.repository.AuditEventRepository;
import com.umdc.backoffice.jpa.repository.ManagedClientAuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceImplTest {

    // Constants to prevent PMD AvoidDuplicateLiterals
    private static final String TEST_IP      = "127.0.0.1";
    private static final String TEST_AGENT   = "TestAgent/1.0";
    private static final String TEST_DETAILS = "{\"key\":\"value\"}";

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private ManagedClientAuditEventRepository managedClientAuditEventRepository;

    @Mock
    private AuditEventMapper auditEventMapper;

    private AuditEventServiceImpl auditEventService;

    @BeforeEach
    void setUp() {
        auditEventService = new AuditEventServiceImpl(
                auditEventRepository, managedClientAuditEventRepository, auditEventMapper);
    }

    // ── record() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("record() calls repository.save() exactly once")
    void recordShouldCallSaveOnce() {
        UUID userId = UUID.randomUUID();
        UUID appId  = UUID.randomUUID();

        auditEventService.record(userId, appId, AuditEventType.LOGIN_SUCCESS,
                TEST_IP, TEST_AGENT, null);

        verify(auditEventRepository, times(1)).save(any(AuditEventEntity.class));
    }

    @Test
    @DisplayName("record() persists entity with the correct userId and eventType")
    void recordShouldPersistCorrectEntityFields() {
        UUID userId = UUID.randomUUID();
        UUID appId  = UUID.randomUUID();
        ArgumentCaptor<AuditEventEntity> captor = ArgumentCaptor.forClass(AuditEventEntity.class);

        auditEventService.record(userId, appId, AuditEventType.LOGIN_SUCCESS,
                TEST_IP, TEST_AGENT, TEST_DETAILS);

        verify(auditEventRepository).save(captor.capture());
        AuditEventEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(appId, saved.getApplicationId());
        assertEquals(AuditEventType.LOGIN_SUCCESS, saved.getEventType());
        assertEquals(TEST_IP, saved.getIpAddress());
        assertEquals(TEST_AGENT, saved.getUserAgent());
        assertEquals(TEST_DETAILS, saved.getDetails());
    }

    @Test
    @DisplayName("record() assigns a non-null UUID id to the entity")
    void recordShouldSetNonNullId() {
        UUID userId = UUID.randomUUID();
        ArgumentCaptor<AuditEventEntity> captor = ArgumentCaptor.forClass(AuditEventEntity.class);

        auditEventService.record(userId, null, AuditEventType.LOGIN_FAILURE,
                TEST_IP, TEST_AGENT, null);

        verify(auditEventRepository).save(captor.capture());
        assertNotNull(captor.getValue().getId());
    }

    // ── findEvents() — query routing ───────────────────────────────────────────

    @Test
    @DisplayName("findEvents with userId only → calls findByUserId")
    void findEventsByUserIdCallsFindByUserId() {
        UUID userId = UUID.randomUUID();
        AuditEventEntity entity = buildEntity(userId, null);
        AuditEventTO to = buildTO(entity);
        when(auditEventRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(auditEventMapper.toTOList(any())).thenReturn(List.of(to));

        ResponseEntity<List<AuditEventTO>> response =
                auditEventService.findEvents(userId, null, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(auditEventRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("findEvents with applicationId only → calls findByApplicationId")
    void findEventsByAppIdCallsFindByApplicationId() {
        UUID appId = UUID.randomUUID();
        AuditEventEntity entity = buildEntity(UUID.randomUUID(), appId);
        AuditEventTO to = buildTO(entity);
        when(auditEventRepository.findByApplicationId(eq(appId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(auditEventMapper.toTOList(any())).thenReturn(List.of(to));

        ResponseEntity<List<AuditEventTO>> response =
                auditEventService.findEvents(null, appId, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(auditEventRepository, times(1)).findByApplicationId(eq(appId), any(Pageable.class));
    }

    @Test
    @DisplayName("findEvents with userId AND applicationId → calls findByUserIdAndApplicationId")
    void findEventsByUserAndAppCallsFindByUserIdAndApplicationId() {
        UUID userId = UUID.randomUUID();
        UUID appId  = UUID.randomUUID();
        AuditEventEntity entity = buildEntity(userId, appId);
        AuditEventTO to = buildTO(entity);
        when(auditEventRepository.findByUserIdAndApplicationId(eq(userId), eq(appId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(auditEventMapper.toTOList(any())).thenReturn(List.of(to));

        ResponseEntity<List<AuditEventTO>> response =
                auditEventService.findEvents(userId, appId, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(auditEventRepository, times(1))
                .findByUserIdAndApplicationId(eq(userId), eq(appId), any(Pageable.class));
    }

    @Test
    @DisplayName("findEvents with all filters null → calls findAll with pageable")
    void findEventsWithNoFiltersCallsFindAll() {
        AuditEventEntity entity = buildEntity(UUID.randomUUID(), null);
        AuditEventTO to = buildTO(entity);
        when(auditEventRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(auditEventMapper.toTOList(any())).thenReturn(List.of(to));

        ResponseEntity<List<AuditEventTO>> response =
                auditEventService.findEvents(null, null, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(auditEventRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("findEvents with empty result → 204 No Content")
    void findEventsReturns204WhenNoResults() {
        when(auditEventRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<List<AuditEventTO>> response =
                auditEventService.findEvents(null, null, null, null, null, 0, 10);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private AuditEventEntity buildEntity(UUID userId, UUID appId) {
        return new AuditEventEntity(UUID.randomUUID(), userId, appId,
                AuditEventType.LOGIN_SUCCESS, TEST_IP, TEST_AGENT, null);
    }

    private AuditEventTO buildTO(AuditEventEntity entity) {
        return new AuditEventTO(entity.getId(), entity.getUserId(), entity.getApplicationId(),
                entity.getEventType(), entity.getIpAddress(), entity.getUserAgent(),
                LocalDateTime.now(), null, LocalDateTime.now());
    }
}

