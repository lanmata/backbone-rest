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

import java.util.Optional;
import java.util.UUID;

/**
 * User-by-id lookup with the full lazy association graph eagerly loaded. See
 * {@link UserGraphLookupServiceImpl} for why this exists instead of
 * {@code UserRepository#findById}.
 */
public interface UserGraphLookupService {

    /**
     * @param userId the user ID
     * @return the matching user, with {@code application}, {@code person}/{@code person.contacts}
     *         and {@code applicationRoleUser}/{@code role}/{@code application} all eagerly loaded
     */
    Optional<UserEntity> findByIdWithGraph(UUID userId);
}
