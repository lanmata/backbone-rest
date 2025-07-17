/*
 *  @(#)ProfileImageController.java
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

package com.prx.backoffice.v1.profileimage.api.controller;

import com.prx.backoffice.v1.profileimage.service.ProfileImageService;
import com.prx.backoffice.v1.profileimage.to.PostProfileImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * ProfileImageController implements the ProfileImageApi interface for profile image operations.
 */
@RestController
@RequestMapping("/api/v1/profile/image")
public class ProfileImageController implements ProfileImageApi {

    private final ProfileImageService profileImageService;

    @Autowired
    public ProfileImageController(ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PostProfileImageResponse> uploadProfileImage(String token, UUID applicationId, byte[] image) throws Exception {
        // Implementation logic goes here
        return profileImageService.save(token, applicationId, image);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<byte[]> getProfileImage(String token) throws Exception {
        // Implementation logic goes here
        return ResponseEntity.ok(new byte[0]);
    }
}

