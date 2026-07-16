/*
 *  @(#)NoticeApi.java
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
package com.umdc.backoffice.v1.notices.api.controller;

import com.umdc.backoffice.v1.notices.api.to.Notice;
import com.umdc.backoffice.v1.notices.api.to.NoticeRequest;
import com.umdc.backoffice.v1.notices.service.NoticeService;
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the Notice API.
 * Provides endpoints for managing notices.
 * <p>
 * A notice has no surrogate id and nothing mutable besides its existence, so
 * there is no single-item lookup and no update operation, only creation,
 * listing by application and deletion by composite key.
 * </p>
 */
@Tag(name = "notices", description = "The notice API")
public interface NoticeApi {

    /**
     * Gets the notice service.
     *
     * @return the notice service
     */
    default NoticeService getService() {
        return new NoticeService() {
        };
    }

    /**
     * Creates a new notice.
     *
     * @param noticeRequest the notice creation request
     * @return the created notice wrapped in a ResponseEntity
     */
    @Operation(summary = "Create a notice", description = "Creates a new notice.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Notice created"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Notice> createNotice(
            @Parameter(description = "Notice creation request", required = true)
            @RequestBody NoticeRequest noticeRequest) {
        return this.getService().create(noticeRequest.getNotice());
    }

    /**
     * Lists notices by application.
     *
     * @param applicationId the application UUID
     * @return the notices wrapped in a ResponseEntity
     */
    @Operation(summary = "List notices by application", description = "Returns every notice registered for the given application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Notice list returned"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Application not found")
    })
    @GetMapping(value = "/application/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<Notice>> listNoticesByApplication(
            @Parameter(description = "Application UUID", required = true)
            @PathVariable UUID applicationId) {
        return this.getService().listByApplication(applicationId);
    }

    /**
     * Deletes a notice identified by its composite key.
     *
     * @param userId        the user UUID
     * @param applicationId the application UUID
     * @param noticeTypeId  the notice type UUID
     * @return a ResponseEntity reflecting the outcome of the deletion
     */
    @Operation(summary = "Delete a notice",
            description = "Addressed by its composite key — there is no surrogate id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Notice deleted"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Notice not found")
    })
    @DeleteMapping(value = "/user/{userId}/application/{applicationId}/notice-type/{noticeTypeId}")
    default ResponseEntity<?> deleteNotice(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Application UUID", required = true)
            @PathVariable UUID applicationId,
            @Parameter(description = "Notice type UUID", required = true)
            @PathVariable UUID noticeTypeId) {
        return this.getService().delete(userId, applicationId, noticeTypeId);
    }
}
