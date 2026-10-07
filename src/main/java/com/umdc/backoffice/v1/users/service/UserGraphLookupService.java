/*
 *  @(#)UserGraphLookupService.java
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

import com.umdc.persistence.general.domains.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User lookups with the full lazy association graph eagerly loaded. See
 * {@link UserGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code UserRepository} query methods.
 */
public interface UserGraphLookupService {

    /**
     * @param userId the user ID
     * @return the matching user, with {@code application}, {@code person}/{@code person.contacts}
     *         and {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded
     */
    Optional<UserEntity> findByIdWithGraph(UUID userId);

    /**
     * @param alias         the user alias
     * @param applicationId the application the user must belong to
     * @return the matching user, with {@code application}, {@code person}/{@code person.contacts}
     *         and {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded
     */
    Optional<UserEntity> findByAliasAndApplicationWithGraph(String alias, UUID applicationId);

    /**
     * @param email         the user email
     * @param applicationId the application the user must belong to
     * @return the matching user, with {@code application}, {@code person}/{@code person.contacts}
     *         and {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded
     */
    Optional<UserEntity> findByEmailAndApplicationWithGraph(String email, UUID applicationId);

    /**
     * @param alias the user alias, not scoped to any application
     * @return the matching user, with {@code application}, {@code person}/{@code person.contacts}
     *         and {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded
     */
    Optional<UserEntity> findByAliasWithGraph(String alias);

    /**
     * @param applicationId the application ID
     * @return every user linked to that application (via {@code applicationRoleUser}), with
     *         {@code application}, {@code person}/{@code person.contacts} and
     *         {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded
     */
    List<UserEntity> findByApplicationWithGraph(UUID applicationId);
}
