/*
 *  @(#)ManagedClientRotationServiceImpl.java
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
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientSecretRotateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of {@link ManagedClientRotationService} providing secret rotation
 * with a Redis-backed grace period for the MCAM feature (Phase 4).
 */
@Service
public class ManagedClientRotationServiceImpl implements ManagedClientRotationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientRotationServiceImpl.class);

    private final ManagedClientRepository repository;
    private final ManagedClientSecretHashService secretHashService;
    private final ManagedClientRedisService redisService;
    private final ManagedClientAuditService auditService;
    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constructs a new {@code ManagedClientRotationServiceImpl}.
     *
     * @param repository         the managed client JPA repository
     * @param secretHashService  the BCrypt hashing service
     * @param redisService       the Redis token lifecycle service
     * @param auditService       the audit event recording service
     * @param securityProperties the security configuration properties
     */
    public ManagedClientRotationServiceImpl(ManagedClientRepository repository,
                                            ManagedClientSecretHashService secretHashService,
                                            ManagedClientRedisService redisService,
                                            ManagedClientAuditService auditService,
                                            SecurityProperties securityProperties) {
        this.repository = repository;
        this.secretHashService = secretHashService;
        this.redisService = redisService;
        this.auditService = auditService;
        this.securityProperties = securityProperties;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<?> rotateSecret(UUID clientId, String requestorIp) {
        LOGGER.debug("Secret rotation requested for clientId='{}'", clientId);

        Optional<ManagedClientEntity> found = repository.findByIdAndActiveTrue(clientId);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ManagedClientErrorResponse(
                            "not_found", "Managed client not found or inactive.", clientId.toString()));
        }

        ManagedClientEntity entity = found.get();
        String currentSecretHash = entity.getSecretHash();


        byte[] rawBytes = new byte[32];
        secureRandom.nextBytes(rawBytes);
        String newRawSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);

        String newHash;
        try {
            newHash = secretHashService.hashSecret(newRawSecret).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while hashing new secret for clientId='{}'", clientId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ManagedClientErrorResponse("server_error", "Secret hashing interrupted.", null));
        } catch (ExecutionException ex) {
            LOGGER.error("Failed to hash new secret for clientId='{}'", clientId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ManagedClientErrorResponse("server_error", "Secret hashing failed.", null));
        }

        long gracePeriodSeconds = securityProperties.getManagementAuthenticator()
                .getRotationGracePeriodSeconds();
        redisService.storeGraceSecret(clientId, currentSecretHash, gracePeriodSeconds);

        entity.setSecretHash(newHash);
        entity.setPrevSecretHash(currentSecretHash);
        entity.setSecretLastRotatedAt(LocalDateTime.now());
        repository.save(entity);

        String details = "{\"gracePeriodSeconds\":" + gracePeriodSeconds + "}";
        auditService.record(clientId, AuditEventType.CLIENT_SECRET_ROTATED, requestorIp, "SUCCESS", details);

        ManagedClientSecretRotateResponse response = new ManagedClientSecretRotateResponse();
        response.setClientId(clientId);
        response.setClientSecret(newRawSecret);
        response.setGracePeriodSeconds(gracePeriodSeconds);
        response.setRotatedAt(LocalDateTime.now());

        LOGGER.debug("Secret rotated for clientId='{}'", clientId);
        return ResponseEntity.ok(response);
    }
}
