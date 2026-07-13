/*
 *  @(#)AuditEventTO.java
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
package com.umdc.backoffice.v1.iam.audit.api.to;

import com.umdc.backoffice.constant.types.AuditEventType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable transfer object representing a single audit event returned by the API.
 *
 * @param id            unique event identifier
 * @param userId        identifier of the user who triggered the event
 * @param applicationId application context (may be {@code null})
 * @param eventType     discriminator for the security event
 * @param ipAddress     client IPv4/IPv6 address (may be {@code null})
 * @param userAgent     client user-agent string (may be {@code null})
 * @param occurredAt    timestamp when the event occurred
 * @param details       JSON string with extra event details (may be {@code null})
 * @param createdAt     timestamp when the record was persisted
 */
public record AuditEventTO(
        UUID id,
        UUID userId,
        UUID applicationId,
        AuditEventType eventType,
        String ipAddress,
        String userAgent,
        LocalDateTime occurredAt,
        String details,
        LocalDateTime createdAt
) {
}

