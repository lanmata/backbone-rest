/*
 *  @(#)AuditEventRepository.java
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
package com.umdc.backoffice.jpa.repository;

import com.umdc.backoffice.jpa.domain.AuditEventEntity;
import com.umdc.backoffice.constant.types.AuditEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link AuditEventEntity}.
 * <p>
 * Provides paginated query methods scoped by user, application, event type
 * and time range. All methods return a {@link Page} to avoid unbounded result sets.
 * </p>
 */
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    /**
     * Returns all audit events for the given user, paginated.
     *
     * @param userId   the user identifier
     * @param pageable pagination parameters
     * @return a page of matching audit events
     */
    Page<AuditEventEntity> findByUserId(UUID userId, Pageable pageable);

    /**
     * Returns all audit events for the given application, paginated.
     *
     * @param applicationId the application identifier
     * @param pageable      pagination parameters
     * @return a page of matching audit events
     */
    Page<AuditEventEntity> findByApplicationId(UUID applicationId, Pageable pageable);

    /**
     * Returns all audit events for the given user within the given application, paginated.
     *
     * @param userId        the user identifier
     * @param applicationId the application identifier
     * @param pageable      pagination parameters
     * @return a page of matching audit events
     */
    Page<AuditEventEntity> findByUserIdAndApplicationId(UUID userId, UUID applicationId, Pageable pageable);

    /**
     * Returns all audit events of the given type, paginated.
     *
     * @param eventType the audit event type
     * @param pageable  pagination parameters
     * @return a page of matching audit events
     */
    Page<AuditEventEntity> findByEventType(AuditEventType eventType, Pageable pageable);

    /**
     * Returns all audit events whose {@code occurredAt} falls within the given range, paginated.
     *
     * @param from     inclusive lower bound
     * @param to       inclusive upper bound
     * @param pageable pagination parameters
     * @return a page of matching audit events
     */
    Page<AuditEventEntity> findByOccurredAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}

