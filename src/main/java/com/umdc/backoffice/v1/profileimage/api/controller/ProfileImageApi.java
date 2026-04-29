/*
 *  @(#)ProfileImageApi.java
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

package com.umdc.backoffice.v1.profileimage.api.controller;

import com.umdc.backoffice.v1.profileimage.service.ProfileImageService;
import com.umdc.backoffice.v1.profileimage.to.GetProfileImageReferenceResponse;
import com.umdc.backoffice.v1.profileimage.to.PostProfileImageResponse;
import com.prx.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.umdc.backoffice.v1.session.services.SessionJwtService.SESSION_TOKEN_KEY;

/**
 * ProfileImageApi defines the API operations for managing profile images.
 */
public interface ProfileImageApi {

    default ProfileImageService getService() {
        return new ProfileImageService() {
        };
    }

    /**
     * Uploads a profile image for a user.
     *
     * @param token of the user
     * @param image the image file as a byte array
     * @return ResponseEntity with upload status
     * @throws Exception if upload fails
     */
    @Operation(summary = "Upload profile image", description = "Uploads a profile image for a user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Image uploaded successfully"),
        @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid input data"),
        @ApiResponse(responseCode = HttpStatusUtil.INTERNAL_SERVER_ERROR_STR, description = "Server error")
    })
    @PostMapping(value = "/application/{applicationId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    default ResponseEntity<PostProfileImageResponse> uploadProfileImage(
            @Parameter(description = "Token session", required = true) @RequestHeader(SESSION_TOKEN_KEY) String token,
            @Parameter(description = "Application Id", required = true) @PathVariable("applicationId") UUID applicationId,
            @Parameter(description = "Profile image file", required = true) @RequestPart byte[] image
    ) throws Exception {
        return ResponseEntity.status(HttpStatusUtil.NOT_IMPLEMENTED).body(new PostProfileImageResponse(""));
    }

    /**
     * Retrieves a user's profile image.
     *
     * @return ResponseEntity with the image as a byte array
     * @throws Exception if retrieval fails
     */
    @Operation(summary = "Get profile image", description = "Retrieves a user's profile image.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Image retrieved successfully"),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Image not found"),
        @ApiResponse(responseCode = HttpStatusUtil.INTERNAL_SERVER_ERROR_STR, description = "Server error")
    })
    @GetMapping("/")
    default ResponseEntity<byte[]> getProfileImage(
            @Parameter(description = "Token session", required = true) @RequestHeader(SESSION_TOKEN_KEY) String token
    ) throws Exception {
        return ResponseEntity.status(HttpStatusUtil.NOT_IMPLEMENTED).body(new byte[0]);
    }

    /**
     * Retrieves the profile image reference for a user.
     *
     * @param token the session token of the user
     * @param applicationId the ID of the application
     * @return ResponseEntity with the profile image reference
     * @throws Exception if retrieval fails
     */
    @Operation(summary = "Get profile image reference", description = "Retrieves the profile image reference for a user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Profile image reference retrieved successfully"),
        @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Profile image reference not found"),
        @ApiResponse(responseCode = HttpStatusUtil.UNAUTHORIZED_STR, description = "Unauthorized access"),
        @ApiResponse(responseCode = HttpStatusUtil.INTERNAL_SERVER_ERROR_STR, description = "Server error")
    })
    @GetMapping(value = "/application/{applicationId}/reference", produces = {MediaType.APPLICATION_JSON_VALUE})
    default ResponseEntity<GetProfileImageReferenceResponse> getProfileImageReference(
            @Parameter(description = "Token session", required = true) @RequestHeader(SESSION_TOKEN_KEY) String token,
            @Parameter(description = "Application Id", required = true) @PathVariable("applicationId") UUID applicationId
    ) throws Exception {
        return ResponseEntity.status(HttpStatusUtil.NOT_IMPLEMENTED).body(null);
    }
}
