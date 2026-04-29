/*
 *  @(#)ProfileImageService.java
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

package com.umdc.backoffice.v1.profileimage.service;

import com.umdc.backoffice.v1.profileimage.to.GetProfileImageReferenceResponse;
import com.umdc.backoffice.v1.profileimage.to.PostProfileImageResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * ProfileImageService provides operations for managing profile images.
 */
public interface ProfileImageService {

    /**
     * Saves a profile image for a user.
     *
     * @param token the ID of the user
     * @param applicationId the ID of the application
     * @param image the image file as a byte array
     * @return the ProfileImageService instance
     * @throws Exception if saving fails
     */
    default ResponseEntity<PostProfileImageResponse> save(String token, UUID applicationId, byte[] image) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieves the profile image reference for a user.
     *
     * @param token the session token of the user
     * @param applicationId the ID of the application
     * @return ResponseEntity with the profile image reference
     * @throws Exception if retrieval fails
     */
    default ResponseEntity<GetProfileImageReferenceResponse> getProfileImageReference(String token, UUID applicationId) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }
}
