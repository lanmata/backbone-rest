/*
 *  @(#)NoticeTypeApi.java
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
package com.umdc.backoffice.v1.noticetypes.api.controller;

import com.umdc.backoffice.v1.noticetypes.api.to.NoticeType;
import com.umdc.backoffice.v1.noticetypes.api.to.NoticeTypeRequest;
import com.umdc.backoffice.v1.noticetypes.service.NoticeTypeService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the Notice Type API.
 * Provides endpoints for managing notice types.
 */
@Tag(name = "notice-types", description = "The notice type API")
public interface NoticeTypeApi {

    /**
     * Gets the notice type service.
     *
     * @return the notice type service
     */
    default NoticeTypeService getService() {
        return new NoticeTypeService() {
        };
    }

    /**
     * Creates a new notice type.
     *
     * @param noticeTypeRequest the notice type creation request
     * @return the created notice type wrapped in a ResponseEntity
     */
    @Operation(summary = "Create a notice type", description = "Creates a new notice type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Notice type created"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/")
    default ResponseEntity<NoticeType> createNoticeType(
            @Parameter(description = "Notice type creation request", required = true)
            @RequestBody NoticeTypeRequest noticeTypeRequest) {
        return getService().create(noticeTypeRequest.getNoticeType());
    }

    /**
     * Finds a notice type by its ID.
     *
     * @param noticeTypeId the unique identifier of the notice type
     * @return the found notice type wrapped in a ResponseEntity
     */
    @Operation(summary = "Find notice type by ID", description = "Returns a single notice type by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Notice type found"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Notice type not found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{noticeTypeId}")
    default ResponseEntity<NoticeType> findNoticeTypeById(
            @Parameter(description = "Notice type UUID", required = true)
            @PathVariable UUID noticeTypeId) {
        return getService().find(noticeTypeId);
    }

    /**
     * Updates an existing notice type.
     *
     * @param noticeTypeId      the unique identifier of the notice type to update
     * @param noticeTypeRequest the notice type update request
     * @return the updated notice type wrapped in a ResponseEntity
     */
    @Operation(summary = "Update a notice type", description = "Updates an existing notice type by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Notice type updated"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Notice type not found")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, path = "/{noticeTypeId}")
    default ResponseEntity<NoticeType> updateNoticeType(
            @Parameter(description = "Notice type UUID", required = true)
            @PathVariable UUID noticeTypeId,
            @Parameter(description = "Updated notice type properties", required = true)
            @RequestBody NoticeTypeRequest noticeTypeRequest) {
        return getService().update(noticeTypeId, noticeTypeRequest.getNoticeType());
    }

    /**
     * Deletes a notice type by its ID.
     *
     * @param noticeTypeId the unique identifier of the notice type to delete
     * @return the deleted notice type wrapped in a ResponseEntity
     */
    @Operation(summary = "Delete a notice type", description = "Deletes the notice type identified by the given UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Notice type deleted"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Notice type not found")
    })
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{noticeTypeId}")
    default ResponseEntity<NoticeType> deleteNoticeType(
            @Parameter(description = "Notice type UUID", required = true)
            @PathVariable UUID noticeTypeId) {
        return getService().delete(noticeTypeId, null);
    }

    /**
     * Returns all registered notice types.
     *
     * @return all notice types wrapped in a ResponseEntity
     */
    @Operation(summary = "List all notice types", description = "Returns every registered notice type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Notice type list returned")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list-all")
    default ResponseEntity<List<NoticeType>> listAllNoticeTypes() {
        return getService().listAll();
    }
}
