/*
 *  @(#)AuditController.java
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
package com.umdc.backoffice.v1.iam.audit.api.controller;

import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// Thin REST controller for IAM audit operations.
/// All business logic is delegated to {@link AuditEventService}.
@RestController
@RequestMapping("/api/v1/iam/audit")
@CrossOrigin(origins = "*")
public class AuditController implements AuditApi {

    private final AuditEventService auditEventService;

    /// Constructs a new {@code AuditController}.
    ///
    /// @param auditEventService the service that handles audit event queries
    public AuditController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /** {@inheritDoc} */
    @Override
    public AuditEventService getService() {
        return auditEventService;
    }
}
