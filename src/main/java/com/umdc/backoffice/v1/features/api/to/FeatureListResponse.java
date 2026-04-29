/*
 *  @(#)FeatureListResponse.java
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
import com.prx.commons.general.pojo.Feature;
import com.prx.commons.general.to.Response;

import java.util.List;

/**
 * FeatureListResponse.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 11-03-2021
 */
@JsonNaming
public class FeatureListResponse extends Response {

    private List<Feature> list;

    /**
     * Default Constructor
     */
    public FeatureListResponse() {
        super();
        // Default Constructor
    }

    public List<Feature> getList() {
        return list;
    }

    public void setList(List<Feature> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "FeatureListResponse{" +
                "list=" + list +
                '}';
    }

}
