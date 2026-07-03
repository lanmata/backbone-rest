/*
 *  @(#)AuditEventServiceImpl.java
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
package com.umdc.backoffice.v1.iam.audit.service;

import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.jpa.domain.AuditEventEntity;
import com.umdc.backoffice.jpa.domain.ManagedClientAuditEventEntity;
import com.umdc.backoffice.jpa.repository.AuditEventRepository;
import com.umdc.backoffice.jpa.repository.ManagedClientAuditEventRepository;
import com.umdc.backoffice.v1.iam.audit.api.to.AuditEventTO;
import com.umdc.backoffice.v1.iam.audit.mapper.AuditEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/// Implementation of {@link AuditEventService}.
/// <p>
/// {@link #saveRecord} is annotated with {@link Async} so that audit writes never
/// block the authentication critical path. All other methods are synchronous.
/// </p>
@Service
public class AuditEventServiceImpl implements AuditEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventServiceImpl.class);

    private static final Set<AuditEventType> M2M_EVENT_TYPES = Set.of(
            AuditEventType.CLIENT_REGISTERED, AuditEventType.CLIENT_UPDATED,
            AuditEventType.CLIENT_DEACTIVATED, AuditEventType.CLIENT_DELETED,
            AuditEventType.CLIENT_SECRET_ROTATED, AuditEventType.CLIENT_TOKEN_ISSUED,
            AuditEventType.CLIENT_TOKEN_ISSUE_FAILED, AuditEventType.CLIENT_TOKEN_REVOKED,
            AuditEventType.CLIENT_INTROSPECTION_CALLED
    );

    private final AuditEventRepository auditEventRepository;
    private final ManagedClientAuditEventRepository managedClientAuditEventRepository;
    private final AuditEventMapper auditEventMapper;

    /// Constructs a new {@code AuditEventServiceImpl}.
    ///
    /// @param auditEventRepository              the repository used to persist IAM audit events
    /// @param managedClientAuditEventRepository the repository used to query M2M audit events
    /// @param auditEventMapper                  the mapper used to convert entities to TOs
    public AuditEventServiceImpl(AuditEventRepository auditEventRepository,
                                  ManagedClientAuditEventRepository managedClientAuditEventRepository,
                                  AuditEventMapper auditEventMapper) {
        this.auditEventRepository = auditEventRepository;
        this.managedClientAuditEventRepository = managedClientAuditEventRepository;
        this.auditEventMapper = auditEventMapper;
    }

    /**
     * {@inheritDoc}
     * Saves an audit event record asynchronously to the database.
     * This method logs the event details and persists the data using the audit event repository.
     *
     * @param userId        The unique identifier of the user associated with the event.
     * @param applicationId The unique identifier of the application triggering the event.
     * @param eventType     The type of the audit event being recorded.
     * @param ipAddress     The IP address from which the event was triggered.
     * @param userAgent     The user agent string related to the event.
     * @param details       Additional details or metadata about the event.
     */
    @Async
    @Override
    public void saveRecord(UUID userId, UUID applicationId, AuditEventType eventType,
                           String ipAddress, String userAgent, String details) {
        LOGGER.debug("Recording audit event — userId='{}', eventType='{}'", userId, eventType);
        try {
            AuditEventEntity entity = new AuditEventEntity(
                    UUID.randomUUID(), userId, applicationId,
                    eventType, ipAddress, userAgent, details
            );
            auditEventRepository.save(entity);
            LOGGER.debug("Audit event persisted — id='{}', eventType='{}'", entity.getId(), eventType);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist audit event — userId='{}', eventType='{}'", userId, eventType, ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<AuditEventTO>> findEvents(UUID userId, UUID applicationId,
                                                          AuditEventType eventType,
                                                          LocalDateTime from, LocalDateTime to,
                                                          int page, int size) {
        LOGGER.debug("findEvents — userId='{}', applicationId='{}', eventType='{}', from='{}', to='{}'",
                userId, applicationId, eventType, from, to);

        Pageable pageable = PageRequest.of(page, size, Sort.by("occurredAt").descending());

        // M2M routing: delegate to ManagedClientAuditEventRepository for M2M event types
        if (eventType != null && M2M_EVENT_TYPES.contains(eventType)) {
            Page<ManagedClientAuditEventEntity> m2mPage =
                    managedClientAuditEventRepository.findByEventType(eventType, pageable);
            if (m2mPage.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<AuditEventTO> m2mTOs = m2mPage.getContent().stream()
                    .map(this::toAuditEventTO)
                    .toList();
            return ResponseEntity.ok(m2mTOs);
        }

        List<AuditEventEntity> results = resolveQuery(userId, applicationId, eventType, from, to, pageable);

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(auditEventMapper.toTOList(results));
    }

    /// Maps a {@link ManagedClientAuditEventEntity} to an {@link AuditEventTO}.
    /// {@code clientId} is surfaced as {@code userId} (field reuse — both represent the acting principal).
    /// {@code applicationId} and {@code userAgent} are not tracked for M2M events.
    private AuditEventTO toAuditEventTO(ManagedClientAuditEventEntity entity) {
        return new AuditEventTO(
                entity.getId(),
                entity.getClientId(),
                null,
                entity.getEventType(),
                entity.getIpAddress(),
                null,
                entity.getOccurredAt(),
                entity.getDetails(),
                entity.getOccurredAt()
        );
    }

    /// Applies the most specific filter combination available and falls back to
    /// progressively less specific queries when filter parameters are absent.
    private List<AuditEventEntity> resolveQuery(UUID userId, UUID applicationId,
                                                 AuditEventType eventType,
                                                 LocalDateTime from, LocalDateTime to,
                                                 Pageable pageable) {
        // Time-range filter takes precedence when both bounds are supplied
        if (from != null && to != null) {
            return toList(auditEventRepository.findByOccurredAtBetween(from, to, pageable));
        }

        // User + application
        if (userId != null && applicationId != null) {
            return toList(auditEventRepository.findByUserIdAndApplicationId(userId, applicationId, pageable));
        }

        // Single-dimension filters
        if (userId != null) {
            return toList(auditEventRepository.findByUserId(userId, pageable));
        }
        if (applicationId != null) {
            return toList(auditEventRepository.findByApplicationId(applicationId, pageable));
        }
        if (eventType != null) {
            return toList(auditEventRepository.findByEventType(eventType, pageable));
        }

        // No filters — return all paginated
        return toList(auditEventRepository.findAll(pageable));
    }

    private List<AuditEventEntity> toList(Page<AuditEventEntity> page) {
        List<AuditEventEntity> list = new ArrayList<>();
        page.forEach(list::add);
        return list;
    }
}

