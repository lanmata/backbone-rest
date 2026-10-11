/*
 *  @(#)FeatureService.java
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

import com.umdc.backoffice.util.PageResponse;
import com.umdc.backoffice.v1.features.api.to.FeatureRoleLink;
import com.umdc.backoffice.v1.features.api.to.FeatureSummary;
import com.umdc.commons.services.CrudService;
import com.umdc.commons.general.pojo.Feature;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * FeatureService.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
public interface FeatureService extends CrudService <UUID, Feature> {

    /**
     * Create a feature.
     *
     * @param feature {@link Feature} object type
     * @return {@link ResponseEntity}<{@link Feature}> object type.
     */
    @Override
    default ResponseEntity<Feature> create(Feature feature) {
        throw new NotImplementedException();
    }

    /**
     * Search one or more {@link Feature} by feature id collection and a flag status (active or inactive).
     *
     * @param featureIds {@link List} object type.
     * @param includeInactive {@link boolean}
     * @return {@link ResponseEntity}<{@link List}<{@link Feature}>> object type.
     */
    default ResponseEntity<List<Feature>> list(List<String> featureIds, boolean includeInactive) {
        throw new NotImplementedException();
    }

    /**
     * Update a feature.
     *
     * @param id {@link String} object type.
     * @param feature {@link Feature} object type.
     * @return {@link ResponseEntity}<{@link Feature}> object type.
     */
    @Override
    default ResponseEntity<Feature> update(UUID id, Feature feature) {
        throw new NotImplementedException();
    }

    /**
     * Delete a feature.
     *
     * @param id {@link String} object type.
     * @param feature {@link Feature} object type.
     * @return {@link ResponseEntity}<{@link Feature}> object type.
     */
    @Override
    default ResponseEntity<Feature> delete(UUID id, Feature feature) {
        throw new NotImplementedException();
    }

    /**
     * Get a feature collection by a feature id collection.
     *
     * @param id {@link String} object type vararg.
     * @return {@link ResponseEntity}<{@link List}<{@link Feature}>> object type.
     */
    @Override
    default ResponseEntity<List<Feature>> list(UUID... id) {
        throw new NotImplementedException();
    }

    /**
     * Get a feature collection linked to a role id.
     *
     * @param roleId {@link UUID} object type.
     * @return {@link ResponseEntity}<{@link List}<{@link Feature}>> object type.
     */
    default ResponseEntity<List<Feature>> listByRole(UUID roleId) {
        throw new NotImplementedException();
    }

    /**
     * Paginated, filtered feature catalog (lightweight summaries). Use it to pick features while creating a
     * role; never list the whole catalog.
     *
     * @param q      optional case-insensitive "contains" filter on name or description
     * @param active optional active filter
     * @param page   zero-based page index
     * @param size   page size (capped by the service)
     * @return the requested {@link PageResponse}
     */
    default ResponseEntity<PageResponse<FeatureSummary>> search(String q, Boolean active, int page, int size) {
        throw new NotImplementedException();
    }

    /**
     * Roles (and their applications) a feature is linked to, paginated.
     *
     * @param featureId the feature id
     * @param page      zero-based page index
     * @param size      page size (capped by the service)
     * @return the requested {@link PageResponse}
     */
    default ResponseEntity<PageResponse<FeatureRoleLink>> listRoles(UUID featureId, int page, int size) {
        throw new NotImplementedException();
    }
}
