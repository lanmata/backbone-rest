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

import com.prx.commons.services.CrudService;
import com.prx.commons.general.pojo.Feature;
import org.apache.commons.lang.NotImplementedException;
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
}
