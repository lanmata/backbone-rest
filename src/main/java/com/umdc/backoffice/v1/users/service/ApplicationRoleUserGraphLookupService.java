/*
 *  @(#)ApplicationRoleUserGraphLookupService.java
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

package com.umdc.backoffice.v1.users.service;

import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code ApplicationRoleUserEntity} lookups with {@code user}, {@code application} and
 * {@code role} (including the role's own {@code roleFeatures}/{@code roleFeatures.feature})
 * all eagerly loaded. See {@link ApplicationRoleUserGraphLookupServiceImpl} for why this exists
 * instead of {@code ApplicationRoleUserRepository#findByUserAndApplication}.
 */
public interface ApplicationRoleUserGraphLookupService {

    /**
     * @param userId        the user ID
     * @param applicationId the application ID
     * @return the matching link, with {@code user}, {@code application}, {@code role} and the
     *         role's own {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<ApplicationRoleUserEntity> findByUserAndApplicationWithGraph(UUID userId, UUID applicationId);
}
