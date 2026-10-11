/*
 *  @(#)FeatureController.java
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
package com.umdc.backoffice.v1.features.api.controller;

import com.umdc.backoffice.v1.features.api.to.FeatureRequest;
import com.umdc.backoffice.v1.features.service.FeatureService;
import com.umdc.backoffice.util.PageResponse;
import com.umdc.backoffice.v1.features.api.to.FeatureRoleLink;
import com.umdc.backoffice.v1.features.api.to.FeatureSummary;
import com.umdc.commons.general.pojo.Feature;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * FeatureController.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@RestController
@RequestMapping(value = "/api/v1/features")
public class FeatureController implements FeatureApi {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @Override
    public FeatureService getService() {
        return this.featureService;
    }

    @Override
    public ResponseEntity<Feature> find(final UUID featureId) {
        return featureService.find(featureId);
    }

    @Override
    public ResponseEntity<List<Feature>> list(boolean includeInactive) {
        return featureService.list(null, includeInactive);
    }

    @Override
    public ResponseEntity<List<Feature>> list(boolean includeInactive, List<String> featuresIds) {
        return featureService.list(featuresIds, includeInactive);
    }

    @Override
    public ResponseEntity<Feature> create(FeatureRequest featureRequest) {
        return featureService.create(featureRequest.getFeature());
    }

    @Override
    public ResponseEntity<Feature> update(UUID featureId, FeatureRequest featureRequest) {
        return featureService.update(featureId, featureRequest.getFeature());
    }

    @Override
    public ResponseEntity<List<Feature>> listByRole(UUID roleId) {
        return featureService.listByRole(roleId);
    }

    @Override
    public ResponseEntity<PageResponse<FeatureSummary>> search(String q, Boolean active, int page, int size) {
        return featureService.search(q, active, page, size);
    }

    @Override
    public ResponseEntity<PageResponse<FeatureRoleLink>> listRoles(UUID featureId, int page, int size) {
        return featureService.listRoles(featureId, page, size);
    }
}
