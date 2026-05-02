/*
 *  @(#)FeatureResponse.java
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

package com.umdc.backoffice.v1.features.api.to;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.umdc.commons.general.pojo.Feature;
import com.umdc.commons.general.to.Response;

/**
 * FeatureResponse.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@JsonNaming
public class FeatureResponse extends Response {
    private Feature feature;

    /**
     * Default constructor
     */
    public FeatureResponse() {
        super();
        // Default constructor
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    @Override
    public String toString() {
        return "FeatureResponse{" +
                "feature=" + feature +
                '}';
    }
}
