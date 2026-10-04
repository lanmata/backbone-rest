/*
 *  @(#)RoleGraphLookupService.java
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

package com.umdc.backoffice.v1.roles.service;

import com.umdc.persistence.general.domains.RoleEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Role lookups with the full lazy association graph eagerly loaded. See
 * {@link RoleGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code RoleRepository} query methods.
 */
public interface RoleGraphLookupService {

    /**
     * @param roleId the role ID
     * @return the matching role, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<RoleEntity> findByIdWithGraph(UUID roleId);

    /**
     * @return every role, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<List<RoleEntity>> findAllWithGraph();

    /**
     * @param ids the role IDs
     * @return the matching roles, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<List<RoleEntity>> findByIdsWithGraph(List<UUID> ids);

    /**
     * @param status the active/inactive flag to filter by
     * @return the matching roles, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<List<RoleEntity>> findByStatusWithGraph(Boolean status);

    /**
     * @param active the active/inactive flag to filter by
     * @param ids    the role IDs to filter by
     * @return the matching roles, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<List<RoleEntity>> findByStatusAndIdsWithGraph(boolean active, List<UUID> ids);

    /**
     * @param userId the user ID
     * @return the roles linked to that user, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<List<RoleEntity>> findByUserIdWithGraph(UUID userId);

    /**
     * @param applicationId the application ID
     * @return the roles belonging to that application, with {@code application} and
     *         {@code roleFeatures}/{@code roleFeatures.feature} all eagerly loaded
     */
    Optional<List<RoleEntity>> findByApplicationIdWithGraph(UUID applicationId);
}
