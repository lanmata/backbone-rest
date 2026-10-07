/*
 *  @(#)SessionUserLookupService.java
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

package com.umdc.backoffice.v1.session.services;

import com.umdc.persistence.general.domains.UserEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * User lookups scoped to an application, with the full {@code applicationRoleUser} /
 * {@code role} / {@code application} graph eagerly loaded. See
 * {@link SessionUserLookupServiceImpl} for why this exists instead of the equivalent
 * {@code UserRepository} query methods.
 */
public interface SessionUserLookupService {

    /**
     * @param alias         the user alias
     * @param applicationId the application scoping the lookup
     * @return the matching user, if any
     */
    Optional<UserEntity> findByAliasAndApplication(String alias, UUID applicationId);

    /**
     * @param email         the user email
     * @param applicationId the application scoping the lookup
     * @return the matching user, if any
     */
    Optional<UserEntity> findByEmailAndApplication(String email, UUID applicationId);

    /**
     * @param userId the user ID
     * @return the matching user, with {@code person}, {@code application} and
     *         {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded —
     *         same graph as the alias/email lookups, used instead of
     *         {@code UserRepository#findUserInfo} for the identical reason
     */
    Optional<UserEntity> findByIdWithGraph(UUID userId);

    /**
     * @param alias the user alias, not scoped to any application
     * @return the matching user, with the same graph eagerly loaded as the other lookups in this
     *         interface — used instead of {@code UserRepository#findByAlias}
     */
    Optional<UserEntity> findByAliasWithGraph(String alias);
}
