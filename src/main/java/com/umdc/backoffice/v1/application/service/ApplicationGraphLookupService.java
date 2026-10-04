/*
 *  @(#)ApplicationGraphLookupService.java
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

package com.umdc.backoffice.v1.application.service;

import com.umdc.persistence.general.domains.ApplicationEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application lookups with the {@code applicationRoleUser} EAGER collection suppressed to lazy.
 * See {@link ApplicationGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code ApplicationRepository} query methods.
 */
public interface ApplicationGraphLookupService {

    /**
     * @param applicationId the application ID
     * @return the matching application, safe to load under native-image
     */
    Optional<ApplicationEntity> findByIdSafe(UUID applicationId);

    /**
     * @param ids the application IDs
     * @return the matching applications, safe to load under native-image
     */
    List<ApplicationEntity> findByIdsSafe(List<UUID> ids);

    /**
     * @return every application, safe to load under native-image
     */
    List<ApplicationEntity> findAllSafe();
}
