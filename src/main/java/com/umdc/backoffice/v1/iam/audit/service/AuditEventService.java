/*
 *  @(#)AuditEventService.java
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

import com.umdc.backoffice.v1.iam.audit.api.to.AuditEventTO;
import com.umdc.backoffice.constant.types.AuditEventType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/// Service contract for persisting and querying security audit events.
public interface AuditEventService {

    /// Records a new audit event asynchronously so that the caller's transaction
    /// is not delayed.
    ///
    /// @param userId        the user who triggered the event (must not be {@code null})
    /// @param applicationId the application context (may be {@code null})
    /// @param eventType     the event discriminator (must not be {@code null})
    /// @param ipAddress     client IP address (may be {@code null})
    /// @param userAgent     client user-agent string (may be {@code null})
    /// @param details       JSON string with additional details (may be {@code null})
    void record(UUID userId, UUID applicationId, AuditEventType eventType,
                String ipAddress, String userAgent, String details);

    /// Queries audit events with optional filters. Passing {@code null} for any
    /// filter parameter disables filtering on that dimension.
    ///
    /// @param userId        optional user filter
    /// @param applicationId optional application filter
    /// @param eventType     optional event-type filter
    /// @param from          optional lower bound on {@code occurredAt}
    /// @param to            optional upper bound on {@code occurredAt}
    /// @param page          zero-based page index
    /// @param size          page size
    /// @return a {@link ResponseEntity} containing the matching events
    ResponseEntity<List<AuditEventTO>> findEvents(UUID userId, UUID applicationId,
                                                   AuditEventType eventType,
                                                   LocalDateTime from, LocalDateTime to,
                                                   int page, int size);
}

