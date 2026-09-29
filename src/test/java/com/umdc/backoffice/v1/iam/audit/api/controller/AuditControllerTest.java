/*
 *  @(#)AuditControllerTest.java
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

import com.umdc.backoffice.v1.iam.audit.api.to.AuditEventTO;
import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import com.umdc.commons.general.pojo.AuditEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link AuditController} together with the default {@code getEvents}
 * method it inherits from {@link AuditApi} — the controller itself only wires
 * {@link AuditController#getService()}, so the query-parameter parsing lives
 * entirely in the interface's default method.
 *
 * @author Luis Mata
 */
class AuditControllerTest {

    @Mock
    private AuditEventService auditEventService;

    private AuditController auditController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditController = new AuditController(auditEventService);
        when(auditEventService.findEvents(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of()));
    }

    @Test
    @DisplayName("getService returns the injected AuditEventService")
    void getServiceReturnsInjectedService() {
        assertEquals(auditEventService, auditController.getService());
    }

    @Test
    @DisplayName("getEvents with no filters delegates with nulls and default paging")
    void getEventsWithNoFiltersUsesDefaults() {
        ResponseEntity<List<AuditEventTO>> response = auditController.getEvents(null, null, null, null, null, 0, 20);

        assertEquals(200, response.getStatusCode().value());
        verify(auditEventService).findEvents(null, null, null, null, null, 0, 20);
    }

    @Test
    @DisplayName("getEvents parses a lowercase eventType into its enum constant")
    void getEventsParsesLowercaseEventType() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        auditController.getEvents(userId, applicationId, "login_success", from, to, 1, 50);

        verify(auditEventService).findEvents(userId, applicationId, AuditEventType.LOGIN_SUCCESS, from, to, 1, 50);
    }

    @Test
    @DisplayName("getEvents treats a blank eventType the same as no filter")
    void getEventsTreatsBlankEventTypeAsNull() {
        auditController.getEvents(null, null, "  ", null, null, 0, 20);

        verify(auditEventService).findEvents(null, null, null, null, null, 0, 20);
    }
}
