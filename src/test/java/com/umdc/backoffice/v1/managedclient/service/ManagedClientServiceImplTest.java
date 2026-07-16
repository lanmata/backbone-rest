package com.umdc.backoffice.v1.managedclient.service;

import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTO;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import com.umdc.backoffice.v1.managedclient.mapper.ManagedClientMapper;
import com.umdc.commons.general.pojo.AuditEventType;
import com.umdc.persistence.general.domains.ManagedClientEntity;
import com.umdc.persistence.general.repositories.ManagedClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for {@link ManagedClientServiceImpl} covering CRUD operations and AC-REG-01,
/// AC-REG-02, AC-SEC-02 acceptance criteria.
@ExtendWith(MockitoExtension.class)
class ManagedClientServiceImplTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_HASH       = "$2a$10$testHashValue";
    private static final String TEST_NAME       = "test-client";
    private static final String TEST_SCOPE      = "read:data";
    private static final String ERR_CONFLICT    = "client_conflict";
    private static final String ERR_NOT_FOUND   = "not_found";

    @Mock
    private ManagedClientRepository repository;

    @Mock
    private ManagedClientMapper mapper;

    @Mock
    private ManagedClientAuditService auditService;

    @Mock
    private ManagedClientSecretHashService secretHashService;

    @Mock
    private ManagedClientTokenService managedClientTokenService;

    private ManagedClientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ManagedClientServiceImpl(
                repository, mapper, auditService, secretHashService, managedClientTokenService);
    }

    // ── registerClient ────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerClient — valid request returns 201 with clientSecret (AC-REG-01)")
    void registerClient_happyPath_returns201WithSecret() {
        UUID appId = UUID.randomUUID();
        ManagedClientCreateRequest request = buildCreateRequest(TEST_NAME, appId);
        ManagedClientEntity entity = buildEntity(UUID.randomUUID(), TEST_NAME, appId);

        when(repository.existsByNameAndApplicationId(TEST_NAME, appId)).thenReturn(false);
        when(secretHashService.hashSecret(any())).thenReturn(CompletableFuture.completedFuture(TEST_HASH));
        when(mapper.toEntity(request)).thenReturn(entity);

        ResponseEntity<?> response = service.registerClient(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ManagedClientCreateResponse body = (ManagedClientCreateResponse) response.getBody();
        assertNotNull(body);
        assertNotNull(body.getClientSecret());
        assertNotNull(body.getClientId());
        verify(repository, times(1)).save(any(ManagedClientEntity.class));
        verify(auditService, times(1)).save(any(UUID.class), eq(AuditEventType.CLIENT_REGISTERED),
                any(), any(), any());
    }

    @Test
    @DisplayName("registerClient — duplicate name+appId returns 409 with client_conflict (AC-REG-02)")
    void registerClient_duplicate_returns409() {
        UUID appId = UUID.randomUUID();
        ManagedClientCreateRequest request = buildCreateRequest(TEST_NAME, appId);
        when(repository.existsByNameAndApplicationId(TEST_NAME, appId)).thenReturn(true);

        ResponseEntity<?> response = service.registerClient(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ManagedClientErrorResponse body = (ManagedClientErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals(ERR_CONFLICT, body.getError());
        verify(repository, never()).save(any());
    }

    // ── listClients ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("listClients — non-empty page returns 200")
    void listClients_withResults_returns200() {
        ManagedClientEntity entity = buildEntity(UUID.randomUUID(), TEST_NAME, UUID.randomUUID());
        when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toTOList(any())).thenReturn(List.of(buildTO(entity)));

        ResponseEntity<?> response = service.listClients(null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("listClients — empty page returns 204")
    void listClients_empty_returns204() {
        when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<?> response = service.listClients(null, null, 0, 20);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("listClients — applicationId filter calls findByApplicationId")
    void listClients_filterByApplicationId_callsCorrectRepository() {
        UUID appId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(UUID.randomUUID(), TEST_NAME, appId);
        when(repository.findByApplicationId(eq(appId), any())).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toTOList(any())).thenReturn(List.of(buildTO(entity)));

        ResponseEntity<?> response = service.listClients(appId, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository, times(1)).findByApplicationId(eq(appId), any());
    }

    // ── getClient ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getClient — found returns 200 with TO that has no secret fields (AC-SEC-02)")
    void getClient_found_returns200WithNoSecretFields() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId, TEST_NAME, UUID.randomUUID());
        ManagedClientTO to = buildTO(entity);
        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(mapper.toTO(entity)).thenReturn(to);

        ResponseEntity<?> response = service.getClient(clientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientTO body = (ManagedClientTO) response.getBody();
        assertNotNull(body);
        assertNoSecretFields(body);
    }

    @Test
    @DisplayName("getClient — not found returns 404 with not_found error")
    void getClient_notFound_returns404() {
        UUID clientId = UUID.randomUUID();
        when(repository.findById(clientId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.getClient(clientId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ManagedClientErrorResponse body = (ManagedClientErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals(ERR_NOT_FOUND, body.getError());
    }

    // ── updateClient ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateClient — partial update persists entity and returns 200")
    void updateClient_partialUpdate_returns200() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId, TEST_NAME, UUID.randomUUID());
        ManagedClientUpdateRequest request = new ManagedClientUpdateRequest();
        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(mapper.toTO(entity)).thenReturn(buildTO(entity));

        ResponseEntity<?> response = service.updateClient(clientId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository, times(1)).save(entity);
        verify(auditService, times(1)).save(eq(clientId), eq(AuditEventType.CLIENT_UPDATED),
                any(), any(), any());
    }

    @Test
    @DisplayName("updateClient — deactivating active client triggers revokeAllTokens")
    void updateClient_deactivate_triggersRevoke() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId, TEST_NAME, UUID.randomUUID());
        entity.setActive(true);
        ManagedClientUpdateRequest request = new ManagedClientUpdateRequest();
        request.setActive(Boolean.FALSE);
        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(mapper.toTO(entity)).thenReturn(buildTO(entity));
        when(managedClientTokenService.revokeAllTokens(clientId)).thenReturn(ResponseEntity.noContent().build());

        service.updateClient(clientId, request);

        verify(managedClientTokenService, times(1)).revokeAllTokens(clientId);
        verify(auditService).save(eq(clientId), eq(AuditEventType.CLIENT_DEACTIVATED), any(), any(), any());
    }

    @Test
    @DisplayName("updateClient — not found returns 404")
    void updateClient_notFound_returns404() {
        UUID clientId = UUID.randomUUID();
        when(repository.findById(clientId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.updateClient(clientId, new ManagedClientUpdateRequest());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── deleteClient ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteClient — found revokes tokens, deletes entity, returns 204")
    void deleteClient_found_triggersRevokeAndDelete() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildEntity(clientId, TEST_NAME, UUID.randomUUID());
        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(managedClientTokenService.revokeAllTokens(clientId)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<?> response = service.deleteClient(clientId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(managedClientTokenService, times(1)).revokeAllTokens(clientId);
        verify(repository, times(1)).delete(entity);
        verify(auditService).save(eq(clientId), eq(AuditEventType.CLIENT_DELETED), any(), any(), any());
    }

    @Test
    @DisplayName("deleteClient — not found returns 404 without deleting")
    void deleteClient_notFound_returns404() {
        UUID clientId = UUID.randomUUID();
        when(repository.findById(clientId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.deleteClient(clientId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repository, never()).delete(any(ManagedClientEntity.class));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientCreateRequest buildCreateRequest(String name, UUID appId) {
        ManagedClientCreateRequest req = new ManagedClientCreateRequest();
        req.setName(name);
        req.setApplicationId(appId);
        req.setScopes(List.of(TEST_SCOPE));
        req.setActive(true);
        return req;
    }

    private ManagedClientEntity buildEntity(UUID id, String name, UUID appId) {
        ManagedClientEntity entity = new ManagedClientEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setApplicationId(appId);
        entity.setScopes(List.of(TEST_SCOPE));
        entity.setActive(true);
        entity.setSecretHash(TEST_HASH);
        return entity;
    }

    private ManagedClientTO buildTO(ManagedClientEntity entity) {
        ManagedClientTO to = new ManagedClientTO();
        to.setClientId(entity.getId());
        to.setName(entity.getName());
        to.setApplicationId(entity.getApplicationId());
        to.setScopes(entity.getScopes());
        to.setActive(entity.isActive());
        return to;
    }

    /// Asserts that {@link ManagedClientTO} does not expose secret fields (AC-SEC-02).
    private void assertNoSecretFields(ManagedClientTO to) {
        assertNotNull(to.getClientId());
        assertNotNull(to.getName());
        boolean hasSecret = Arrays.stream(ManagedClientTO.class.getDeclaredFields())
                .anyMatch(f -> "secretHash".equals(f.getName()) || "prevSecretHash".equals(f.getName()));
        assertFalse(hasSecret, "ManagedClientTO must not expose secretHash or prevSecretHash");
    }
}
