/*
 *  @(#)ManagedClientAuditService.java
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

import java.util.UUID;

/// Service interface for recording managed client audit events.
public interface ManagedClientAuditService {

    /// Records an audit event for a managed client lifecycle action.
    ///
    /// @param clientId  the UUID of the managed client
    /// @param eventType the type of audit event
    /// @param ipAddress the source IP address, or {@code null} if not applicable
    /// @param outcome   the result — {@code "SUCCESS"} or {@code "FAILURE"}
    /// @param details   optional JSON detail payload, or {@code null}
    void record(UUID clientId, AuditEventType eventType, String ipAddress,
                String outcome, String details);
}
