/*
 *  @(#)ManagedClientServiceImpl.java
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

import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.jpa.domain.ManagedClientEntity;
import com.umdc.backoffice.jpa.repository.ManagedClientRepository;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import com.umdc.backoffice.v1.managedclient.mapper.ManagedClientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/// Implementation of {@link ManagedClientService} providing admin CRUD operations
/// for managed clients (MCAM feature).
@Service
public class ManagedClientServiceImpl implements ManagedClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientServiceImpl.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String OUTCOME_SUCCESS = "SUCCESS";

    private final ManagedClientRepository repository;
    private final ManagedClientMapper mapper;
    private final ManagedClientAuditService auditService;
    private final ManagedClientSecretHashService secretHashService;
    private final ManagedClientTokenService managedClientTokenService;

    /// Constructs a new {@code ManagedClientServiceImpl}.
    ///
    /// @param repository                the managed client JPA repository
    /// @param mapper                    the MapStruct mapper for entity ↔ DTO conversion
    /// @param auditService              the audit service for recording lifecycle events
    /// @param secretHashService         the service for BCrypt hashing of client secrets
    /// @param managedClientTokenService the token service for revoking active tokens
    public ManagedClientServiceImpl(ManagedClientRepository repository,
                                    ManagedClientMapper mapper,
                                    ManagedClientAuditService auditService,
                                    ManagedClientSecretHashService secretHashService,
                                    ManagedClientTokenService managedClientTokenService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.secretHashService = secretHashService;
        this.managedClientTokenService = managedClientTokenService;
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<?> registerClient(ManagedClientCreateRequest request) {
        LOGGER.debug("Registering managed client — name='{}', applicationId='{}'",
                request.getName(), request.getApplicationId());

        if (repository.existsByNameAndApplicationId(request.getName(), request.getApplicationId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ManagedClientErrorResponse(
                            "client_conflict",
                            "A client with this name already exists for the application.",
                            null));
        }

        UUID clientId = UUID.randomUUID();
        String rawSecret = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(SECURE_RANDOM.generateSeed(32));

        String secretHash;
        try {
            secretHash = secretHashService.hashSecret(rawSecret).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while hashing secret for clientId='{}'", clientId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ManagedClientErrorResponse("server_error", "Secret hashing interrupted.", null));
        } catch (ExecutionException ex) {
            LOGGER.error("Failed to hash secret for clientId='{}'", clientId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ManagedClientErrorResponse("server_error", "Secret hashing failed.", null));
        }

        ManagedClientEntity entity = mapper.toEntity(request);
        entity.setId(clientId);
        entity.setSecretHash(secretHash);
        entity.setActive(request.isActive());

        repository.save(entity);

        auditService.record(clientId, AuditEventType.CLIENT_REGISTERED, null, OUTCOME_SUCCESS, null);

        ManagedClientCreateResponse response = new ManagedClientCreateResponse();
        response.setClientId(clientId);
        response.setClientSecret(rawSecret);
        response.setName(entity.getName());
        response.setApplicationId(entity.getApplicationId());
        response.setScopes(entity.getScopes());
        response.setActive(entity.isActive());
        response.setCreatedAt(entity.getCreatedAt());

        LOGGER.debug("Managed client registered — clientId='{}'", clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<?> listClients(UUID applicationId, Boolean active, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<ManagedClientEntity> result;

        if (applicationId != null && active != null) {
            result = repository.findByApplicationIdAndActive(applicationId, active, pageable);
        } else if (applicationId != null) {
            result = repository.findByApplicationId(applicationId, pageable);
        } else if (active != null) {
            result = repository.findByActive(active, pageable);
        } else {
            result = repository.findAll(pageable);
        }

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mapper.toTOList(result.getContent()));
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<?> getClient(UUID clientId) {
        Optional<ManagedClientEntity> found = repository.findById(clientId);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ManagedClientErrorResponse(
                            "not_found",
                            "Managed client not found.",
                            clientId.toString()));
        }
        return ResponseEntity.ok(mapper.toTO(found.get()));
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<?> updateClient(UUID clientId, ManagedClientUpdateRequest request) {
        Optional<ManagedClientEntity> found = repository.findById(clientId);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ManagedClientErrorResponse(
                            "not_found",
                            "Managed client not found.",
                            clientId.toString()));
        }

        ManagedClientEntity entity = found.get();

        // If deactivating: revoke all active tokens before applying the change (FR-17)
        if (Boolean.FALSE.equals(request.getActive()) && entity.isActive()) {
            managedClientTokenService.revokeAllTokens(entity.getId());
            auditService.record(entity.getId(), AuditEventType.CLIENT_DEACTIVATED, null, OUTCOME_SUCCESS, null);
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
        auditService.record(clientId, AuditEventType.CLIENT_UPDATED, null, OUTCOME_SUCCESS, null);

        return ResponseEntity.ok(mapper.toTO(entity));
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<?> deleteClient(UUID clientId) {
        Optional<ManagedClientEntity> found = repository.findById(clientId);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ManagedClientErrorResponse(
                            "not_found",
                            "Managed client not found.",
                            clientId.toString()));
        }

        // Revoke all active tokens before deletion (FR-30)
        managedClientTokenService.revokeAllTokens(found.get().getId());

        repository.delete(found.get());
        auditService.record(clientId, AuditEventType.CLIENT_DELETED, null, OUTCOME_SUCCESS, null);

        return ResponseEntity.noContent().build();
    }
}
