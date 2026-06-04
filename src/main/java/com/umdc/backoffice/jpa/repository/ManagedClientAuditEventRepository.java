/*
 *  @(#)ManagedClientAuditEventRepository.java
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

import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.jpa.domain.ManagedClientAuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/// Spring Data JPA repository for {@link ManagedClientAuditEventEntity}.
/// <p>
/// Supports paginated querying of M2M lifecycle audit events by client ID,
/// event type, or both. Used by {@code ManagedClientAuditServiceImpl} and
/// by {@code AuditEventServiceImpl} when routing M2M event type queries.
/// </p>
public interface ManagedClientAuditEventRepository
        extends JpaRepository<ManagedClientAuditEventEntity, UUID> {

    /// Returns a paginated list of audit events for a specific managed client.
    ///
    /// @param clientId the managed client UUID
    /// @param pageable pagination and sort parameters
    /// @return a page of matching audit event entities
    Page<ManagedClientAuditEventEntity> findByClientId(UUID clientId, Pageable pageable);

    /// Returns a paginated list of audit events for a given event type.
    ///
    /// @param type     the {@link AuditEventType} discriminator
    /// @param pageable pagination and sort parameters
    /// @return a page of matching audit event entities
    Page<ManagedClientAuditEventEntity> findByEventType(AuditEventType type, Pageable pageable);

    /// Returns a paginated list of audit events filtered by both client and event type.
    ///
    /// @param clientId the managed client UUID
    /// @param type     the {@link AuditEventType} discriminator
    /// @param pageable pagination and sort parameters
    /// @return a page of matching audit event entities
    Page<ManagedClientAuditEventEntity> findByClientIdAndEventType(
            UUID clientId, AuditEventType type, Pageable pageable);
}

