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

package com.prx.backoffice.v1.profileimage.service;

import com.prx.backoffice.util.JwtUtil;
import com.prx.backoffice.v1.profileimage.to.PostProfileImageResponse;
import com.prx.commons.util.DateUtil;
import com.prx.commons.util.HttpStatusUtil;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * ProfileImageServiceImpl implements the ProfileImageService interface for profile image operations.
 */
@Service
public class ProfileImageServiceImpl implements ProfileImageService {
    private final ApplicationRoleUserRepository applicationRoleUserRepository;

    public ProfileImageServiceImpl(ApplicationRoleUserRepository applicationRoleUserRepository) {
        this.applicationRoleUserRepository = applicationRoleUserRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PostProfileImageResponse> save(String token, UUID applicationId, byte[] image) throws Exception {
        // 1. Extract alias/userId from the token (assuming JWT, adjust as needed)
        UUID userId = JwtUtil.getUidFromToken(token);

        var result = applicationRoleUserRepository.findByUserAndApplication(userId, applicationId);
        if (Objects.nonNull(result)) {
            // 2. Generate filename: alias-uuid-yyyyMMddHHmmss.jpg
            String date = new SimpleDateFormat(DateUtil.PATTERN_DATETIME_YYMMDDHHMMSS, Locale.ROOT).format(new java.util.Date());
            String filename = userId + "-" + date + ".jpg";
            // 3. Define a storage path (adjust as needed)

            String applicationRelativePath = result.getApplication().getCodeName();
            Path dirPath = Paths.get("/temp/" + applicationRelativePath);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(filename);

            // 4. Save image to storage
            Files.write(filePath, image);
            result.setProfileImageRef(applicationRelativePath + "/" + filename);
            // 5. Update ApplicationRoleUser.profile_photo_ref (pseudo-code, adjust repository/service as needed)
            applicationRoleUserRepository.save(result);
            // 6. Build response with an image link (assuming the UI client can access /profile-images/{filename})
            return ResponseEntity.ok(new PostProfileImageResponse(applicationRelativePath + "/" + filename));
        }
        return ResponseEntity.status(HttpStatusUtil.NOT_FOUND).body(new PostProfileImageResponse(""));
    }

}
