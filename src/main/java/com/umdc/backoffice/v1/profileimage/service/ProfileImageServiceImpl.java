/*
 *  @(#)ProfileImageServiceImpl.java
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

import com.umdc.backoffice.util.JwtUtil;
import com.umdc.backoffice.v1.profileimage.client.CloudflareR2StorageClient;
import com.umdc.backoffice.v1.profileimage.to.GetProfileImageReferenceResponse;
import com.umdc.backoffice.v1.profileimage.to.PostProfileImageResponse;
import com.prx.commons.util.DateUtil;
import com.prx.commons.util.HttpStatusUtil;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.umdc.backoffice.constant.BackboneAppConstants.*;

/**
 * ProfileImageServiceImpl implements the ProfileImageService interface for profile image operations.
 * This service uses Cloudflare R2 for image storage.
 */
@Service
public class ProfileImageServiceImpl implements ProfileImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileImageServiceImpl.class);
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String PROFILE_IMAGE_PREFIX = "profiles/";
    
    private final ApplicationRoleUserRepository applicationRoleUserRepository;
    private final CloudflareR2StorageClient r2StorageClient;

    /**
     * Constructor for ProfileImageServiceImpl.
     *
     * @param applicationRoleUserRepository repository for application role user data
     * @param r2StorageClient client for Cloudflare R2 storage operations
     */
    public ProfileImageServiceImpl(ApplicationRoleUserRepository applicationRoleUserRepository,
                                   CloudflareR2StorageClient r2StorageClient) {
        this.applicationRoleUserRepository = applicationRoleUserRepository;
        this.r2StorageClient = r2StorageClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PostProfileImageResponse> save(String token, UUID applicationId, byte[] image) throws Exception {
        logger.info("Saving profile image for application: {}", applicationId);
        
        // 1. Extract userId from the JWT token
        UUID userId = JwtUtil.getUidFromToken(token);
        
        if (userId == null) {
            logger.warn("Invalid token: unable to extract user ID");
            return ResponseEntity.status(HttpStatusUtil.UNAUTHORIZED).body(new PostProfileImageResponse(""));
        }

        // 2. Find the application role user record
        var applicationRoleUser = applicationRoleUserRepository.findByUserAndApplication(userId, applicationId);
        
        if (Objects.isNull(applicationRoleUser)) {
            logger.warn("ApplicationRoleUser not found for user: {} and application: {}", userId, applicationId);
            return ResponseEntity.status(HttpStatusUtil.NOT_FOUND).body(new PostProfileImageResponse(""));
        }

        // 3. Generate unique filename: images/{applicationCode}/{userId}-{timestamp}.jpg
        String applicationCode = applicationRoleUser.getApplication().getCodeName();
        String timestamp = new SimpleDateFormat(DateUtil.PATTERN_DATETIME_YYMMDDHHMMSS, Locale.ROOT)
                .format(new java.util.Date());
        String objectKey = applicationCode + "/" + PROFILE_IMAGE_PREFIX + userId + "-" + timestamp + IMAGE_EXTENSION;

        try {
            // 4. Upload image to Cloudflare R2
            String contentType = determineContentType(image);
            r2StorageClient.uploadImage(image, objectKey, contentType);

            // 5. Update the profile image reference in the database
            applicationRoleUser.setProfileImageRef(objectKey);
            applicationRoleUserRepository.save(applicationRoleUser);

            // 6. Return response with public URL or object key
            String publicUrl = r2StorageClient.getPublicUrl(objectKey);
            logger.info("Profile image saved successfully: {}", publicUrl);
            
            return ResponseEntity.ok(new PostProfileImageResponse(publicUrl));
            
        } catch (Exception e) {
            logger.error("Error uploading profile image to R2", e);
            return ResponseEntity.status(HttpStatusUtil.INTERNAL_SERVER_ERROR)
                    .body(new PostProfileImageResponse(""));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<GetProfileImageReferenceResponse> getProfileImageReference(String token, UUID applicationId) 
            throws Exception {
        logger.info("Getting profile image reference for application: {}", applicationId);
        
        // 1. Extract userId from the JWT token
        UUID userId = JwtUtil.getUidFromToken(token);
        
        if (userId == null) {
            logger.warn("Invalid token: unable to extract user ID");
            return ResponseEntity.status(HttpStatusUtil.UNAUTHORIZED).body(null);
        }

        // 2. Find the application role user record
        var applicationRoleUser = applicationRoleUserRepository.findByUserAndApplication(userId, applicationId);
        
        if (Objects.isNull(applicationRoleUser) || applicationRoleUser.getProfileImageRef() == null) {
            logger.warn("Profile image reference not found for user: {} and application: {}", userId, applicationId);
            return ResponseEntity.status(HttpStatusUtil.NOT_FOUND).body(null);
        }

        // 3. Get the public URL for the image
        String imageRef = applicationRoleUser.getProfileImageRef();
        String publicUrl = r2StorageClient.getPublicUrl(imageRef);
        
        logger.info("Profile image reference retrieved: {}", publicUrl);
        return ResponseEntity.ok(new GetProfileImageReferenceResponse(publicUrl));
    }

    /**
     * Determines the content type based on image byte signature.
     *
     * @param imageData the image byte array
     * @return the MIME type string
     */
    private String determineContentType(byte[] imageData) {
        if (imageData == null || imageData.length < 2) {
            return IMAGE_JPEG;
        }

        // Check for common image signatures
        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
            return IMAGE_JPEG;
        } else if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50) {
            return IMAGE_PNG;
        } else if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49) {
            return IMAGE_GIF;
        } else if (imageData[0] == (byte) 0x52 && imageData[1] == (byte) 0x49) {
            return IMAGE_WEBP;
        }

        return IMAGE_JPEG; // Default fallback
    }
}

