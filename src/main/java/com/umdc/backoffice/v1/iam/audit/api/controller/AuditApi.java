/*
 *  @(#)AuditApi.java
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
import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/// REST API interface for querying security audit events.
/// <p>
/// All endpoints require a valid bearer token. Callers without the
/// appropriate role will receive a {@code 403 Forbidden} response from
/// the security filter chain before reaching this controller.
/// </p>
@Tag(name = "iam-audit", description = "IAM Audit Event API")
@RequestMapping("/api/v1/iam/audit")
public interface AuditApi {

    /// Returns the {@link AuditEventService} used by this API.
    ///
    /// @return the service instance
    default AuditEventService getService() {
        return new AuditEventService() {
            @Override
            public void saveRecord(UUID userId, UUID applicationId, AuditEventType eventType,
                                   String ipAddress, String userAgent, String details) {
                // no-op default
            }

            @Override
            public ResponseEntity<List<AuditEventTO>> findEvents(UUID userId, UUID applicationId,
                                                                  AuditEventType eventType,
                                                                  LocalDateTime from, LocalDateTime to,
                                                                  int page, int size) {
                return ResponseEntity.status(HttpStatusUtil.NOT_IMPLEMENTED).build();
            }
        };
    }

    /**
     * Retrieves a paginated list of security audit events based on optional filtering criteria.
     * Omitting a filter parameter will disable filtering on that dimension.
     *
     * @param userId        Optional identifier of the user who triggered the events to filter by.
     * @param applicationId Optional identifier of the application context to filter by.
     * @param eventType     Optional event type to filter by (case-insensitive).
     * @param from          Optional lower bound on the occurrence timestamp of events.
     * @param to            Optional upper bound on the occurrence timestamp of events.
     * @param page          Zero-based index of the page to retrieve (default is 0).
     * @param size          Number of events per page (default is 20).
     * @return A {@link ResponseEntity} containing a list of {@link AuditEventTO} objects that match the filters,
     *         or an appropriate HTTP status code if no events match or there is an error.
     */
    @Operation(
            summary = "Query audit events",
            description = "Returns a paginated list of security audit events. All query parameters are optional; "
                    + "omitting a parameter disables filtering on that dimension."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR,
                    description = "Audit events returned successfully"),
            @ApiResponse(responseCode = HttpStatusUtil.NO_CONTENT_STR,
                    description = "No audit events match the supplied filters"),
            @ApiResponse(responseCode = HttpStatusUtil.UNAUTHORIZED_STR,
                    description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR,
                    description = "Invalid query parameter value")
    })
    @GetMapping("/events")
    default ResponseEntity<List<AuditEventTO>> getEvents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AuditEventType parsedEventType = null;
        if (eventType != null && !eventType.isBlank()) {
            parsedEventType = AuditEventType.valueOf(eventType.toUpperCase(Locale.ROOT));
        }
        return getService().findEvents(userId, applicationId, parsedEventType, from, to, page, size);
    }
}

