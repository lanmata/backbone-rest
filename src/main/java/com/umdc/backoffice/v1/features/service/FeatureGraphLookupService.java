/*
 *  @(#)FeatureGraphLookupService.java
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

package com.umdc.backoffice.v1.features.service;

import com.umdc.persistence.general.domains.FeatureEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Feature lookups with the full lazy association graph eagerly loaded. See
 * {@link FeatureGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code FeatureRepository} query methods.
 */
public interface FeatureGraphLookupService {

    /**
     * @param featureId the feature ID
     * @return the matching feature, with {@code rolFeatures}/{@code rolFeatures.role}/
     *         {@code rolFeatures.role.application} all eagerly loaded
     */
    Optional<FeatureEntity> findByIdWithGraph(UUID featureId);

    /**
     * @param name the feature name
     * @return the matching feature, with the same graph as {@link #findByIdWithGraph}
     */
    Optional<FeatureEntity> findByNameWithGraph(String name);

    /**
     * @return every feature, with the same graph as {@link #findByIdWithGraph}
     */
    List<FeatureEntity> findAllWithGraph();

    /**
     * @param ids    the feature IDs
     * @param active the active/inactive flag to filter by
     * @return the matching features, with the same graph as {@link #findByIdWithGraph}
     */
    List<FeatureEntity> findByIdsAndStatusWithGraph(List<UUID> ids, boolean active);

    /**
     * @param roleId the role ID
     * @return the features linked to that role, with the same graph as {@link #findByIdWithGraph}
     */
    List<FeatureEntity> findByRoleIdWithGraph(UUID roleId);
}
